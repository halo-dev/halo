package run.halo.app.core.extension.service;

import static run.halo.app.core.extension.RoleBinding.containsUser;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ReactiveExtensionClient;

@Service
public class UserServiceImpl implements UserService {

    private final ReactiveExtensionClient client;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(ReactiveExtensionClient client, PasswordEncoder passwordEncoder) {
        this.client = client;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<User> getUser(String username) {
        return client.get(User.class, username);
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

}
