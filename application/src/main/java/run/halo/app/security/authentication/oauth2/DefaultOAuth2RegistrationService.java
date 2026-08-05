package run.halo.app.security.authentication.oauth2;

import java.time.Clock;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.UserConnection;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.ValidationUtils;
import run.halo.app.infra.exception.AgreementNotAcceptedException;

/** Default implementation of {@link OAuth2RegistrationService}. */
@Service
@RequiredArgsConstructor
public class DefaultOAuth2RegistrationService implements OAuth2RegistrationService {

    private static final int RANDOM_USERNAME_MAX_ATTEMPTS = 20;
    private static final String RANDOM_USERNAME_PREFIX = "user-";

    private final ReactiveExtensionClient client;
    private final UserService userService;
    private final UserConnectionService connectionService;
    private final SystemConfigFetcher systemConfigFetcher;
    private final jakarta.validation.Validator validator;
    private final Clock clock;

    @Override
    public Mono<RegistrationResult> register(OAuth2AuthenticationToken token, boolean agreedToTerms) {
        var registrationId = token.getAuthorizedClientRegistrationId();
        var oauth2User = token.getPrincipal();
        return systemConfigFetcher
                .fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
                .filter(SystemSetting.User::isAllowRegistration)
                .switchIfEmpty(Mono.error(
                        () -> new ServerWebInputException("The registration is not allowed by the administrator.")))
                .filter(setting -> StringUtils.isNotBlank(setting.getDefaultRole()))
                .switchIfEmpty(Mono.error(
                        () -> new ServerWebInputException("The default role is not configured by the administrator.")))
                .flatMap(setting -> checkAgreement(setting, agreedToTerms).thenReturn(setting))
                .flatMap(setting -> connectionService
                        .getByProviderUserId(registrationId, oauth2User.getName())
                        .map(connection -> connection.getSpec().getUsername())
                        .switchIfEmpty(Mono.defer(() -> createUser(setting, registrationId, oauth2User)
                                .map(user -> user.getMetadata().getName()))))
                .flatMap(username -> Mono.zip(
                                userService.getUser(username),
                                systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                        .map(tuple -> new RegistrationResult(
                                username,
                                tuple.getT2().isMustVerifyEmailOnRegistration()
                                        && !tuple.getT1().getSpec().isEmailVerified())));
    }

    private Mono<Void> checkAgreement(SystemSetting.User setting, boolean agreedToTerms) {
        if (CollectionUtils.isEmpty(setting.getRequiredAgreementPages()) || agreedToTerms) {
            return Mono.empty();
        }
        return Mono.error(() -> new AgreementNotAcceptedException(
                "Agreement not accepted.", "problemDetail.user.signup.agreement-not-accepted", null));
    }

    private Mono<User> createUser(SystemSetting.User setting, String registrationId, OAuth2User oauth2User) {
        return resolveUsername(setting, oauth2User)
                .flatMap(username -> resolveEmail(oauth2User).flatMap(emailCandidate -> {
                    var user = new User();
                    var metadata = new Metadata();
                    metadata.setName(username);
                    user.setMetadata(metadata);
                    var spec = new User.UserSpec();
                    spec.setDisplayName(resolveDisplayName(setting, oauth2User, username));
                    spec.setEmail(emailCandidate.email());
                    spec.setEmailVerified(emailCandidate.verified() && StringUtils.isNotBlank(emailCandidate.email()));
                    spec.setRegisteredAt(clock.instant());
                    user.setSpec(spec);
                    return userService
                            .createUser(user, Set.of(setting.getDefaultRole()))
                            .flatMap(created -> connectionService
                                    .createUserConnection(username, registrationId, oauth2User)
                                    .onErrorResume(e -> client.delete(created).then(Mono.<UserConnection>error(e)))
                                    .thenReturn(created));
                }));
    }

    private Mono<String> resolveUsername(SystemSetting.User setting, OAuth2User oauth2User) {
        var candidate = firstText(
                attribute(oauth2User, "login"),
                attribute(oauth2User, "username"),
                attribute(oauth2User, "user_name"),
                oauth2User instanceof OidcUser oidcUser ? oidcUser.getPreferredUsername() : null,
                attribute(oauth2User, "nickname"),
                oauth2User.getName());
        return resolveAvailableUsername(setting, candidate)
                .switchIfEmpty(Mono.defer(() -> generateRandomUsername(setting)));
    }

    private Mono<String> resolveAvailableUsername(SystemSetting.User setting, String candidate) {
        if (StringUtils.isBlank(candidate)) {
            return Mono.empty();
        }
        var username = candidate.trim().toLowerCase(Locale.ROOT);
        if (!isValidUsername(username) || !isUsernameAllowed(setting, username)) {
            return Mono.empty();
        }
        return client.fetch(User.class, username)
                .hasElement()
                .flatMap(exists -> exists ? Mono.empty() : Mono.just(username));
    }

    private Mono<String> generateRandomUsername(SystemSetting.User setting) {
        return Flux.range(0, RANDOM_USERNAME_MAX_ATTEMPTS)
                .concatMap(i -> {
                    var username = RANDOM_USERNAME_PREFIX
                            + RandomStringUtils.secure().nextAlphanumeric(8).toLowerCase(Locale.ROOT);
                    if (!isValidUsername(username) || !isUsernameAllowed(setting, username)) {
                        return Mono.empty();
                    }
                    return client.fetch(User.class, username)
                            .hasElement()
                            .filter(exists -> !exists)
                            .map(ignored -> username);
                })
                .next()
                .switchIfEmpty(Mono.error(() -> new ServerWebInputException("Failed to generate a unique username.")));
    }

    private boolean isValidUsername(String username) {
        return username.length() >= 4
                && username.length() <= 63
                && ValidationUtils.NAME_PATTERN.matcher(username).matches();
    }

    private boolean isUsernameAllowed(SystemSetting.User setting, String username) {
        return !protectedNames(setting).contains(username.toLowerCase(Locale.ROOT));
    }

    private boolean isDisplayNameAllowed(SystemSetting.User setting, String displayName) {
        return !protectedNames(setting).contains(displayName.toLowerCase(Locale.ROOT));
    }

    private Set<String> protectedNames(SystemSetting.User setting) {
        var protectedNames = setting.getProtectedUsernames();
        if (StringUtils.isBlank(protectedNames)) {
            return Set.of();
        }
        return java.util.Arrays.stream(protectedNames.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    private String resolveDisplayName(SystemSetting.User setting, OAuth2User oauth2User, String username) {
        var displayName = firstText(
                attribute(oauth2User, "name"),
                attribute(oauth2User, "nickname"),
                attribute(oauth2User, "display_name"),
                oauth2User instanceof OidcUser oidcUser ? oidcUser.getPreferredUsername() : null,
                username);
        if (!isDisplayNameAllowed(setting, displayName)) {
            return username;
        }
        return displayName;
    }

    private Mono<EmailCandidate> resolveEmail(OAuth2User oauth2User) {
        var email = getEmail(oauth2User);
        if (!isValidEmail(email)) {
            return Mono.just(new EmailCandidate(null, false));
        }
        var normalized = email.toLowerCase(Locale.ROOT);
        return userService
                .checkEmailAlreadyVerified(normalized)
                .map(occupied -> occupied
                        ? new EmailCandidate(null, false)
                        : new EmailCandidate(normalized, isVerifiedByDefault(oauth2User)));
    }

    private String getEmail(OAuth2User oauth2User) {
        if (oauth2User instanceof OidcUser oidcUser) {
            return oidcUser.getClaimAsString("email");
        }
        return attribute(oauth2User, "email");
    }

    private boolean isVerifiedByDefault(OAuth2User oauth2User) {
        if (oauth2User instanceof OidcUser oidcUser) {
            return Boolean.TRUE.equals(oidcUser.getClaimAsBoolean("email_verified"));
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        return StringUtils.isNotBlank(email)
                && validator.validate(new EmailForm(email)).isEmpty();
    }

    private static String attribute(OAuth2User oauth2User, String key) {
        var value = oauth2User.getAttributes().get(key);
        return value instanceof String text ? text : null;
    }

    private static String firstText(String... values) {
        for (var value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private record EmailCandidate(String email, boolean verified) {}

    private record EmailForm(
            @jakarta.validation.constraints.Email String email) {}
}
