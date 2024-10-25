package run.halo.app.core.user.service.impl;

import static run.halo.app.extension.ExtensionUtil.defaultSort;
import static run.halo.app.extension.index.query.QueryFactory.equal;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.EmailVerificationService;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.core.user.service.SignUpData;
import run.halo.app.core.user.service.UserPostCreatingHandler;
import run.halo.app.core.user.service.UserPreCreatingHandler;
import run.halo.app.core.user.service.UserService;
import run.halo.app.event.user.PasswordChangedEvent;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.exception.ExtensionNotFoundException;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.ValidationUtils;
import run.halo.app.infra.exception.DuplicateNameException;
import run.halo.app.infra.exception.EmailVerificationFailed;
import run.halo.app.infra.exception.UnsatisfiedAttributeValueException;
import run.halo.app.infra.exception.UserNotFoundException;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    public static final String GHOST_USER_NAME = "ghost";

    private final ReactiveExtensionClient client;

    private final PasswordEncoder passwordEncoder;

    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    private final ApplicationEventPublisher eventPublisher;

    private final RoleService roleService;

    private final EmailVerificationService emailVerificationService;

    private final ExtensionGetter extensionGetter;

    private Clock clock = Clock.systemUTC();

    void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Mono<User> getUser(String username) {
        return client.get(User.class, username)
            .onErrorMap(ExtensionNotFoundException.class, e -> new UserNotFoundException(username));
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
            })
            .doOnNext(user -> publishPasswordChangedEvent(username));
    }

    @Override
    public Mono<User> updateWithRawPassword(String username, String rawPassword) {
        if (!ValidationUtils.PASSWORD_PATTERN.matcher(rawPassword).matches()) {
            return Mono.error(
                new UnsatisfiedAttributeValueException("validation.error.password.pattern"));
        }
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
            })
            .doOnNext(user -> publishPasswordChangedEvent(username));
    }

    @Override
    public Mono<User> grantRoles(String username, Set<String> roles) {
        return client.get(User.class, username)
            .flatMap(user -> {
                var bindingsToUpdate = new HashSet<RoleBinding>();
                var bindingsToDelete = new HashSet<RoleBinding>();
                var existingRoles = new HashSet<String>();
                var subject = new RoleBinding.Subject();
                subject.setKind(User.KIND);
                subject.setApiGroup(User.GROUP);
                subject.setName(username);
                return roleService.listRoleBindings(subject)
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
                            .filter(StringUtils::hasText)
                            .map(roleName -> RoleBinding.create(username, roleName));
                    }).flatMap(client::create))
                    .then(Mono.defer(() -> {
                        var annotations = Optional.ofNullable(user.getMetadata().getAnnotations())
                            .orElseGet(HashMap::new);
                        user.getMetadata().setAnnotations(annotations);
                        annotations.put(User.REQUEST_TO_UPDATE, clock.instant().toString());
                        return client.update(user);
                    }));
            });
    }

    @Override
    public Mono<User> signUp(SignUpData signUpData) {
        return environmentFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
            .filter(SystemSetting.User::isAllowRegistration)
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException(
                "The registration is not allowed by the administrator."
            )))
            .filter(setting -> StringUtils.hasText(setting.getDefaultRole()))
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException(
                "The default role is not configured by the administrator."
            )))
            .flatMap(setting -> {
                var user = new User();
                user.setMetadata(new Metadata());
                var metadata = user.getMetadata();
                metadata.setName(signUpData.getUsername());
                user.setSpec(new User.UserSpec());
                var spec = user.getSpec();
                spec.setPassword(passwordEncoder.encode(signUpData.getPassword()));
                spec.setEmailVerified(false);
                spec.setRegisteredAt(clock.instant());
                spec.setEmail(signUpData.getEmail());
                spec.setDisplayName(signUpData.getDisplayName());
                Mono<Void> verifyEmail = Mono.empty();
                if (setting.isMustVerifyEmailOnRegistration()) {
                    if (!StringUtils.hasText(signUpData.getEmailCode())) {
                        return Mono.error(
                            new EmailVerificationFailed("Email captcha is required", null)
                        );
                    }
                    verifyEmail = emailVerificationService.verifyRegisterVerificationCode(
                            signUpData.getEmail(), signUpData.getEmailCode()
                        )
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(Mono.error(() ->
                            new EmailVerificationFailed("Invalid email captcha.", null)
                        ))
                        .doOnNext(spec::setEmailVerified)
                        .then();
                }
                return verifyEmail.then(Mono.defer(() -> {
                    var defaultRole = setting.getDefaultRole();
                    return createUser(user, Set.of(defaultRole));
                }));
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
            .then(extensionGetter.getExtensions(UserPreCreatingHandler.class)
                .concatMap(handler -> handler.preCreating(user))
                .then(Mono.defer(() -> client.create(user)
                    .flatMap(newUser -> grantRoles(user.getMetadata().getName(), roleNames)
                        .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                            .filter(OptimisticLockingFailureException.class::isInstance)
                        )
                    )
                ))
                .flatMap(createdUser -> extensionGetter.getExtensions(UserPostCreatingHandler.class)
                    .concatMap(handler -> handler.postCreating(createdUser))
                    .then()
                    .thenReturn(createdUser)
                )
            );
    }

    @Override
    public Mono<Boolean> confirmPassword(String username, String rawPassword) {
        return getUser(username)
            .filter(user -> {
                if (!StringUtils.hasText(user.getSpec().getPassword())) {
                    // If the password is not set, return true directly.
                    return true;
                }
                if (!StringUtils.hasText(rawPassword)) {
                    return false;
                }
                return passwordEncoder.matches(rawPassword, user.getSpec().getPassword());
            })
            .hasElement();
    }

    @Override
    public Flux<User> listByEmail(String email) {
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(equal("spec.email", email)));
        return client.listAll(User.class, listOptions, defaultSort());
    }

    @Override
    public String encryptPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    void publishPasswordChangedEvent(String username) {
        eventPublisher.publishEvent(new PasswordChangedEvent(this, username));
    }
}
