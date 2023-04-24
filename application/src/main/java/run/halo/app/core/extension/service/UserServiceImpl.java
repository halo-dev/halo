package run.halo.app.core.extension.service;

import static run.halo.app.core.extension.RoleBinding.containsUser;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.exception.ExtensionNotFoundException;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.AccessDeniedException;
import run.halo.app.infra.exception.DuplicateNameException;
import run.halo.app.infra.exception.UserNotFoundException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    public static final String GHOST_USER_NAME = "ghost";

    private final ReactiveExtensionClient client;

    private final PasswordEncoder passwordEncoder;

    private final SystemConfigurableEnvironmentFetcher environmentFetcher;


    @Override
    public Mono<User> getUser(String username) {
        return client.get(User.class, username)
            .onErrorMap(ExtensionNotFoundException.class,
                e -> new UserNotFoundException(username));
    }

    @Override
    public Mono<User> getUserOrGhost(String username) {
        return client.fetch(User.class, username)
            .switchIfEmpty(Mono.defer(() -> client.get(User.class, GHOST_USER_NAME)));
    }

    @Override
    public Mono<User> updatePassword(String username, String newPassword) {
        return getUser(username)
            .filter(user -> !Objects.equals(user.getSpec().getPassword(), newPassword))
            .flatMap(user -> {
                user.getSpec().setPassword(newPassword);
                return client.update(user);
            });
    }

    @Override
    public Mono<User> updateWithRawPassword(String username, String rawPassword) {
        return getUser(username)
            .filter(user -> {
                if (!StringUtils.hasText(user.getSpec().getPassword())) {
                    // Check if the old password is set before, or the passwordEncoder#matches
                    // will complain an error due to null password.
                    return true;
                }
                return !passwordEncoder.matches(rawPassword, user.getSpec().getPassword());
            })
            .flatMap(user -> {
                user.getSpec().setPassword(passwordEncoder.encode(rawPassword));
                return client.update(user);
            });
    }

    @Override
    public Flux<Role> listRoles(String name) {
        return client.list(RoleBinding.class, containsUser(name), null)
            .filter(roleBinding -> Role.KIND.equals(roleBinding.getRoleRef().getKind()))
            .map(roleBinding -> roleBinding.getRoleRef().getName())
            .flatMap(roleName -> client.fetch(Role.class, roleName));
    }

    @Override
    @Transactional
    public Mono<User> grantRoles(String username, Set<String> roles) {
        return client.get(User.class, username)
            .flatMap(user -> {
                var bindingsToUpdate = new HashSet<RoleBinding>();
                var bindingsToDelete = new HashSet<RoleBinding>();
                var existingRoles = new HashSet<String>();
                return client.list(RoleBinding.class, RoleBinding.containsUser(username), null)
                    .doOnNext(binding -> {
                        var roleName = binding.getRoleRef().getName();
                        if (roles.contains(roleName)) {
                            existingRoles.add(roleName);
                            return;
                        }
                        binding.getSubjects().removeIf(RoleBinding.Subject.isUser(username));
                        if (CollectionUtils.isEmpty(binding.getSubjects())) {
                            // remove it if subjects is empty
                            bindingsToDelete.add(binding);
                        } else {
                            bindingsToUpdate.add(binding);
                        }
                    })
                    .thenMany(Flux.fromIterable(bindingsToUpdate).flatMap(client::update))
                    .thenMany(Flux.fromIterable(bindingsToDelete).flatMap(client::delete))
                    .thenMany(Flux.fromStream(() -> {
                        var mutableRoles = new HashSet<>(roles);
                        mutableRoles.removeAll(existingRoles);
                        return mutableRoles.stream()
                            .map(roleName -> RoleBinding.create(username, roleName));
                    }).flatMap(client::create))
                    .then(Mono.just(user));
            });
    }

    @Override
    @Transactional
    public Mono<User> signUp(User user, String password) {
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        return environmentFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
            .switchIfEmpty(Mono.error(new IllegalStateException("User setting is not configured")))
            .flatMap(userSetting -> {
                Boolean allowRegistration = userSetting.getAllowRegistration();
                if (BooleanUtils.isFalse(allowRegistration)) {
                    return Mono.error(new AccessDeniedException("Registration is not allowed",
                        "problemDetail.user.signUpFailed.disallowed",
                        null));
                }
                String defaultRole = userSetting.getDefaultRole();
                if (!StringUtils.hasText(defaultRole)) {
                    return Mono.error(new AccessDeniedException(
                        "Default registration role is not configured by admin",
                        "problemDetail.user.signUpFailed.disallowed",
                        null));
                }
                String encodedPassword = passwordEncoder.encode(password);
                user.getSpec().setPassword(encodedPassword);
                return createUser(user, Set.of(defaultRole));
            });
    }

    @Override
    public Mono<User> createUser(User user, Set<String> roleNames) {
        Assert.notNull(user, "User must not be null");
        Assert.notNull(roleNames, "Roles must not be null");
        return client.fetch(User.class, user.getMetadata().getName())
            .hasElement()
            .flatMap(hasUser -> {
                if (hasUser) {
                    return Mono.error(
                        new DuplicateNameException("User name is already in use", null,
                            "problemDetail.user.duplicateName",
                            new Object[] {user.getMetadata().getName()}));
                }
                // Check if all roles exist
                return Flux.fromIterable(roleNames)
                    .flatMap(roleName -> client.fetch(Role.class, roleName)
                        .switchIfEmpty(Mono.error(() -> new ServerWebInputException(
                            "Role [" + roleName + "] is not found."))
                        )
                    )
                    .then();
            })
            .then(Mono.defer(() -> client.create(user)
                .flatMap(newUser -> grantRoles(user.getMetadata().getName(), roleNames)))
            );
    }
}
