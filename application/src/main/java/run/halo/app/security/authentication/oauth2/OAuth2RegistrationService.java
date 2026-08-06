package run.halo.app.security.authentication.oauth2;

import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Clock;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.ValidationUtils;
import run.halo.app.infra.exception.AgreementNotAcceptedException;
import run.halo.app.security.authentication.oauth2.OAuth2RegistrationException.Error;

@Service
@RequiredArgsConstructor
public class OAuth2RegistrationService {

    static final int RANDOM_USERNAME_MAX_ATTEMPTS = 20;
    static final String RANDOM_USERNAME_PREFIX = "user-";
    static final String REGISTRATION_CLAIM_ANNO = "auth.halo.run/oauth2-registration-claim";

    private final ReactiveExtensionClient client;
    private final UserService userService;
    private final UserConnectionService connectionService;
    private final SystemConfigFetcher systemConfigFetcher;
    private final Validator validator;
    private Clock clock = Clock.systemUTC();

    public Mono<RegistrationResult> register(OAuth2AuthenticationToken token, boolean agreedToTerms) {
        return fetchAndValidateSettings(agreedToTerms).flatMap(setting -> register(token, setting));
    }

    private Mono<SystemSetting.User> fetchAndValidateSettings(boolean agreedToTerms) {
        return systemConfigFetcher
                .fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
                .filter(SystemSetting.User::isAllowRegistration)
                .switchIfEmpty(Mono.error(() -> new OAuth2RegistrationException(Error.REGISTRATION_CLOSED)))
                .filter(setting -> StringUtils.hasText(setting.getDefaultRole()))
                .switchIfEmpty(Mono.error(() -> new OAuth2RegistrationException(Error.DEFAULT_ROLE_MISSING)))
                .filter(setting -> CollectionUtils.isEmpty(setting.getRequiredAgreementPages()) || agreedToTerms)
                .switchIfEmpty(Mono.error(() -> new AgreementNotAcceptedException(
                        "Agreement not accepted.", "problemDetail.user.signup.agreement-not-accepted", null)));
    }

    private Mono<RegistrationResult> register(OAuth2AuthenticationToken token, SystemSetting.User setting) {
        var registrationId = token.getAuthorizedClientRegistrationId();
        var oauth2User = token.getPrincipal();
        return connectionService
                .getByProviderUserId(registrationId, oauth2User.getName())
                .map(connection -> connection.getSpec().getUsername())
                .switchIfEmpty(Mono.defer(() -> createUserAndConnection(registrationId, oauth2User, setting)))
                .flatMap(username -> userService
                        .getUser(username)
                        .map(user -> new RegistrationResult(
                                username,
                                setting.isMustVerifyEmailOnRegistration()
                                        && !user.getSpec().isEmailVerified())));
    }

    private Mono<String> createUserAndConnection(
            String registrationId, OAuth2User oauth2User, SystemSetting.User setting) {
        return resolveUsername(oauth2User, setting)
                .flatMap(username -> resolveEmail(oauth2User).flatMap(email -> {
                    var claim = UUID.randomUUID().toString();
                    var user = new User();
                    user.setMetadata(new Metadata());
                    user.getMetadata().setName(username);
                    MetadataUtil.nullSafeAnnotations(user).put(REGISTRATION_CLAIM_ANNO, claim);
                    var spec = user.getSpec();
                    spec.setDisplayName(resolveDisplayName(oauth2User, username, setting));
                    spec.setEmail(email.email());
                    spec.setEmailVerified(email.verified());
                    spec.setRegisteredAt(clock.instant());
                    return userService
                            .createUser(user, Set.of(setting.getDefaultRole()))
                            .thenReturn(username)
                            .onErrorResume(original -> compensateOwnedUser(username, claim)
                                    .then(Mono.defer(() ->
                                            existingConnectionOrError(registrationId, oauth2User.getName(), original))))
                            .flatMap(resolvedUsername -> Objects.equals(resolvedUsername, username)
                                    ? createConnection(username, claim, registrationId, oauth2User)
                                    : Mono.just(resolvedUsername));
                }));
    }

    private Mono<String> createConnection(String username, String claim, String registrationId, OAuth2User oauth2User) {
        return connectionService
                .createUserConnection(username, registrationId, oauth2User)
                .thenReturn(username)
                .onErrorResume(original -> compensateOwnedUser(username, claim)
                        .then(Mono.defer(
                                () -> existingConnectionOrError(registrationId, oauth2User.getName(), original))))
                .flatMap(resolvedUsername -> Objects.equals(resolvedUsername, username)
                        ? clearRegistrationClaim(username, claim).thenReturn(username)
                        : Mono.just(resolvedUsername));
    }

    private Mono<String> existingConnectionOrError(String registrationId, String providerUserId, Throwable original) {
        return connectionService
                .getByProviderUserId(registrationId, providerUserId)
                .map(existing -> existing.getSpec().getUsername())
                .switchIfEmpty(Mono.error(original));
    }

    private Mono<Void> compensateOwnedUser(String username, String claim) {
        return client.fetch(User.class, username)
                .filter(user ->
                        Objects.equals(MetadataUtil.nullSafeAnnotations(user).get(REGISTRATION_CLAIM_ANNO), claim))
                .flatMap(client::delete)
                .then();
    }

    private Mono<Void> clearRegistrationClaim(String username, String claim) {
        return client.fetch(User.class, username)
                .filter(user ->
                        Objects.equals(MetadataUtil.nullSafeAnnotations(user).get(REGISTRATION_CLAIM_ANNO), claim))
                .flatMap(user -> {
                    MetadataUtil.nullSafeAnnotations(user).remove(REGISTRATION_CLAIM_ANNO);
                    return client.update(user);
                })
                .then();
    }

    private Mono<String> resolveUsername(OAuth2User oauth2User, SystemSetting.User setting) {
        var candidate = usernameCandidate(oauth2User);
        if (candidate != null && isValidUsername(candidate) && !isProtected(setting, candidate)) {
            return client.fetch(User.class, candidate)
                    .hasElement()
                    .filter(exists -> !exists)
                    .map(notUsed -> candidate)
                    .switchIfEmpty(Mono.defer(this::randomUsername));
        }
        return randomUsername();
    }

    private String usernameCandidate(OAuth2User oauth2User) {
        return firstNonBlank(
                        attribute(oauth2User, "login"),
                        attribute(oauth2User, "username"),
                        attribute(oauth2User, "user_name"),
                        oauth2User instanceof OidcUser oidcUser ? oidcUser.getPreferredUsername() : null,
                        attribute(oauth2User, "nickname"),
                        oauth2User.getName())
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .orElse(null);
    }

    private Mono<String> randomUsername() {
        return Flux.range(0, RANDOM_USERNAME_MAX_ATTEMPTS)
                .concatMap(ignored -> {
                    var username = RANDOM_USERNAME_PREFIX
                            + RandomStringUtils.secure().nextAlphanumeric(8).toLowerCase(Locale.ROOT);
                    return client.fetch(User.class, username)
                            .hasElement()
                            .filter(exists -> !exists)
                            .map(notUsed -> username);
                })
                .next()
                .switchIfEmpty(Mono.error(new ServerWebInputException("Failed to generate a unique username.")));
    }

    private String resolveDisplayName(OAuth2User oauth2User, String username, SystemSetting.User setting) {
        var displayName = firstNonBlank(
                        attribute(oauth2User, "name"),
                        attribute(oauth2User, "nickname"),
                        attribute(oauth2User, "display_name"),
                        oauth2User instanceof OidcUser oidcUser ? oidcUser.getPreferredUsername() : null)
                .map(String::trim)
                .orElse(username);
        return isProtected(setting, displayName) ? username : displayName;
    }

    private Mono<EmailCandidate> resolveEmail(OAuth2User oauth2User) {
        var value = oauth2User instanceof OidcUser oidcUser ? oidcUser.getEmail() : attribute(oauth2User, "email");
        if (value == null) {
            return Mono.just(new EmailCandidate(null, false));
        }
        var email = value.trim().toLowerCase(Locale.ROOT);
        if (!validator.validate(new EmailForm(email)).isEmpty()) {
            return Mono.just(new EmailCandidate(null, false));
        }
        var verified = !(oauth2User instanceof OidcUser oidcUser) || Boolean.TRUE.equals(oidcUser.getEmailVerified());
        return userService
                .checkEmailAlreadyVerified(email)
                .map(occupied -> occupied ? new EmailCandidate(null, false) : new EmailCandidate(email, verified));
    }

    private boolean isValidUsername(String username) {
        return username.length() >= 4
                && username.length() <= 63
                && ValidationUtils.NAME_PATTERN.matcher(username).matches();
    }

    private boolean isProtected(SystemSetting.User setting, String value) {
        var protectedUsernames = setting.getProtectedUsernames();
        if (!StringUtils.hasText(protectedUsernames)) {
            return false;
        }
        var normalized = value.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(protectedUsernames.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet())
                .contains(normalized);
    }

    private static String attribute(OAuth2User oauth2User, String name) {
        return Optional.ofNullable(oauth2User.getAttribute(name))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .orElse(null);
    }

    private static Optional<String> firstNonBlank(String... values) {
        return Stream.of(values)
                .filter(Objects::nonNull)
                .filter(StringUtils::hasText)
                .findFirst();
    }

    void setClock(Clock clock) {
        this.clock = clock;
    }

    public record RegistrationResult(String username, boolean needsEmailCompletion) {}

    private record EmailCandidate(String email, boolean verified) {}

    private record EmailForm(@Email @NotBlank String email) {}
}
