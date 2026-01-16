package run.halo.app.core.user.service.impl;

import static run.halo.app.extension.ExtensionUtil.defaultSort;
import static run.halo.app.extension.index.query.Queries.equal;

import java.time.Clock;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.ReactiveSessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
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
import run.halo.app.extension.index.query.Queries;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.ValidationUtils;
import run.halo.app.infra.exception.DuplicateNameException;
import run.halo.app.infra.exception.EmailAlreadyTakenException;
import run.halo.app.infra.exception.EmailVerificationFailed;
import run.halo.app.infra.exception.RestrictedNameException;
import run.halo.app.infra.exception.UnsatisfiedAttributeValueException;
import run.halo.app.infra.exception.UserNotFoundException;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.security.authorization.AuthorityUtils;
import run.halo.app.security.device.DeviceService;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final ReactiveExtensionClient client;

    private final PasswordEncoder passwordEncoder;

    private final SystemConfigFetcher environmentFetcher;

    private final ApplicationEventPublisher eventPublisher;

    private final RoleService roleService;

    private final EmailVerificationService emailVerificationService;

    private final ExtensionGetter extensionGetter;

    private final DeviceService deviceService;

    private final ReactiveTransactionManager transactionManager;

    private final ReactiveSessionRegistry sessionRegistry;

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
    public Mono<User> findUserByVerifiedEmail(String email) {
        var listOptions = ListOptions.builder()
            .andQuery(equal("spec.emailVerified", true))
            .andQuery(equal("spec.email", email.toLowerCase()))
            .build();
        return client.listAll(User.class, listOptions, defaultSort()).next();
    }

    @Override
    public Mono<User> getUserOrGhost(String username) {
        return client.fetch(User.class, username)
            .switchIfEmpty(Mono.defer(() -> client.get(User.class, GHOST_USER_NAME)));
    }

    @Override
    public Flux<User> getUsersOrGhosts(Collection<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return Flux.empty();
        }
        var nameSet = new HashSet<>(names);
        nameSet.add(GHOST_USER_NAME);
        var options = ListOptions.builder()
            .andQuery(Queries.in("metadata.name", nameSet))
            .build();
        return client.listAll(User.class, options, defaultSort())
            .collectMap(u -> u.getMetadata().getName())
            .map(map -> {
                var ghost = map.get(GHOST_USER_NAME);
                return names.stream()
                    .map(name -> map.getOrDefault(name, ghost))
                    .toList();
            })
            .flatMapMany(Flux::fromIterable);
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
        var bindingsToUpdate = new HashSet<RoleBinding>();
        var bindingsToDelete = new HashSet<RoleBinding>();
        var existingRoles = new HashSet<String>();
        var subject = new RoleBinding.Subject();
        subject.setKind(User.KIND);
        subject.setApiGroup(User.GROUP);
        subject.setName(username);
        var tx = TransactionalOperator.create(transactionManager);
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
            .then(Mono.defer(() -> {
                if (log.isDebugEnabled()) {
                    log.debug("""
                            Updating roles for user {}: existingRoles={}, roles={}, \
                            bindingsToUpdate={}, bindingsToDelete={}""",
                        username, existingRoles, roles, bindingsToUpdate, bindingsToDelete
                    );
                }
                var updateBindings = Flux.fromIterable(bindingsToUpdate)
                    .flatMap(client::update)
                    .then();
                var deleteBindings = Flux.fromIterable(bindingsToDelete)
                    .flatMap(client::delete)
                    .then();
                var createBindings = Flux.fromIterable(roles)
                    .filter(role -> !existingRoles.contains(role))
                    .filter(StringUtils::hasText)
                    .map(role -> RoleBinding.create(username, role))
                    .flatMap(client::create);
                return Mono.when(updateBindings, deleteBindings, createBindings);
            }))
            .as(tx::transactional)
            .then(Mono.defer(() -> {
                if (Objects.equals(roles, existingRoles)) {
                    // No need to update the user if roles are not changed
                    log.debug("No role changes for user {}, skip updating user annotations.",
                        username);
                    return Mono.empty();
                }
                log.info("Updated roles for user {}: existingRoles={}, roles={}",
                    username, existingRoles, roles
                );
                var invalidateSessions = sessionRegistry.getAllSessions(username)
                    .flatMap(reactiveSessionInformation -> {
                        log.info("Invalidating session {} for user {}",
                            reactiveSessionInformation.getSessionId(), username
                        );
                        return reactiveSessionInformation.invalidate();
                    })
                    .then();
                var updateUser = client.get(User.class, username)
                    .doOnNext(u -> {
                        var annotations = u.getMetadata().getAnnotations();
                        if (annotations == null) {
                            annotations = new HashMap<>();
                            u.getMetadata().setAnnotations(annotations);
                        }
                        annotations.put(User.REQUEST_TO_UPDATE, clock.instant().toString());
                    })
                    .flatMap(client::update)
                    .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                        .filter(OptimisticLockingFailureException.class::isInstance)
                    );
                return invalidateSessions.then(updateUser);
            }));
    }

    @Override
    public Mono<Boolean> hasSufficientRoles(Collection<String> roles) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(a -> AuthorityUtils.authoritiesToRoles(a.getAuthorities()))
            .flatMap(userRoles -> roleService.contains(userRoles, roles))
            .defaultIfEmpty(false);
    }

    @Override
    public Mono<User> signUp(SignUpData signUpData) {
        return environmentFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
            .filter(SystemSetting.User::isAllowRegistration)
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException(
                "The registration is not allowed by the administrator."
            )))
            .filter(setting -> isUsernameAllowed(setting, signUpData.getUsername()))
            .switchIfEmpty(Mono.error(RestrictedNameException::new))
            .filter(setting -> StringUtils.hasText(setting.getDefaultRole()))
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException(
                "The default role is not configured by the administrator."
            )))
            .flatMap(setting -> {
                var email = Optional.ofNullable(signUpData.getEmail())
                    .map(String::toLowerCase)
                    .orElse(null);
                var user = new User();
                user.setMetadata(new Metadata());
                var metadata = user.getMetadata();
                metadata.setName(signUpData.getUsername());
                user.setSpec(new User.UserSpec());
                var spec = user.getSpec();
                spec.setPassword(passwordEncoder.encode(signUpData.getPassword()));
                spec.setEmailVerified(false);
                spec.setRegisteredAt(clock.instant());
                spec.setEmail(email);
                spec.setDisplayName(signUpData.getDisplayName());
                Mono<Void> verifyEmail = Mono.empty();
                if (setting.isMustVerifyEmailOnRegistration()) {
                    if (!StringUtils.hasText(email)) {
                        return Mono.error(
                            new EmailVerificationFailed("Email captcha is required", null)
                        );
                    }
                    verifyEmail = emailVerificationService.verifyRegisterVerificationCode(
                            email, signUpData.getEmailCode()
                        )
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(Mono.error(() ->
                            new EmailVerificationFailed("Invalid email captcha.", null)
                        ))
                        .then(this.checkEmailAlreadyVerified(email))
                        .filter(has -> !has)
                        .switchIfEmpty(Mono.error(
                            () -> new EmailAlreadyTakenException("Email is already taken")
                        ))
                        .doOnNext(v -> spec.setEmailVerified(true))
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
                    .flatMap(newUser -> grantRoles(user.getMetadata().getName(), roleNames))
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
        listOptions.setFieldSelector(FieldSelector.of(equal("spec.email", email.toLowerCase())));
        return client.listAll(User.class, listOptions, defaultSort());
    }

    @Override
    public Mono<Boolean> checkEmailAlreadyVerified(String email) {
        return listByEmail(email)
            // TODO Use index query in the future
            .filter(u -> u.getSpec().isEmailVerified())
            .hasElements();
    }

    @Override
    public String encryptPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public Mono<User> disable(String username) {
        var tx = TransactionalOperator.create(transactionManager);
        return client.fetch(User.class, username)
            .filter(user -> !Boolean.TRUE.equals(user.getSpec().getDisabled()))
            .flatMap(user -> deviceService.revoke(username).thenReturn(user))
            .doOnNext(user -> user.getSpec().setDisabled(true))
            .flatMap(client::update)
            .as(tx::transactional);
    }

    @Override
    public Mono<User> enable(String username) {
        return client.fetch(User.class, username)
            .filter(user -> Boolean.TRUE.equals(user.getSpec().getDisabled()))
            .doOnNext(user -> user.getSpec().setDisabled(false))
            .flatMap(client::update);
    }

    void publishPasswordChangedEvent(String username) {
        eventPublisher.publishEvent(new PasswordChangedEvent(this, username));
    }

    private boolean isUsernameAllowed(SystemSetting.User setting, String username) {
        String protectedUsernamesStr = setting.getProtectedUsernames();
        if (protectedUsernamesStr == null || protectedUsernamesStr.trim().isEmpty()) {
            return true;
        }
        Set<String> protectedLowerSet = Arrays.stream(protectedUsernamesStr.split(","))
            .map(String::trim)
            .filter(n -> !n.isEmpty())
            .map(String::toLowerCase)
            .collect(Collectors.toUnmodifiableSet());
        return !protectedLowerSet.contains(username.toLowerCase());
    }
}
