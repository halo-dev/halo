package run.halo.app.security.authentication.token;

import static java.nio.charset.StandardCharsets.UTF_8;
import static run.halo.app.security.PersonalAccessToken.PAT_TOKEN_PREFIX;
import static run.halo.app.security.authorization.AuthorityUtils.*;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.nimbusds.jwt.JWTClaimNames;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.Strings;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.security.PersonalAccessToken;
import run.halo.app.security.authorization.AuthorityUtils;

public class PatAuthenticationManager implements ReactiveAuthenticationManager {

    /** Minimal duration gap of personal access token update. */
    private static final Duration MIN_UPDATE_GAP = Duration.ofMinutes(1);

    private final ReactiveAuthenticationManager delegate;

    private final ReactiveExtensionClient client;

    private final RoleService roleService;

    private Clock clock;

    public PatAuthenticationManager(
            ReactiveExtensionClient client, ReactiveAuthenticationManager delegate, RoleService roleService) {
        this.client = client;
        this.delegate = delegate;
        this.roleService = roleService;
        this.clock = Clock.systemDefaultZone();
    }

    /**
     * Set new clock. Only for testing.
     *
     * @param clock new clock
     */
    void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .cast(BearerTokenAuthenticationToken.class)
                .map(t -> Strings.CS.removeStart(t.getToken(), PAT_TOKEN_PREFIX))
                .flatMap(token -> {
                    if (token.indexOf('.') >= 0) {
                        // Legacy PAT in JWT format
                        return legacyAuthenticate(token);
                    }
                    return authenticateOpaque(token);
                });
    }

    private Mono<Authentication> legacyAuthenticate(String token) {
        return delegate.authenticate(new BearerTokenAuthenticationToken(token))
                .cast(JwtAuthenticationToken.class)
                .flatMap(this::checkAndRebuild)
                .map(auth -> auth);
    }

    private Mono<Authentication> authenticateOpaque(String token) {
        return Mono.defer(() -> {
            var parts = parseToken(token);
            var patName = parts[0];
            var secret = parts[1];
            return client.fetch(PersonalAccessToken.class, patName)
                    .switchIfEmpty(Mono.error(() -> new DisabledException("Personal access token has been deleted.")))
                    .flatMap(pat -> opaquePatChecks(pat, secret)
                            .then(checkUser(
                                    pat.getSpec().getUsername(), pat.getSpec().getRoles()))
                            .then(updateLastUsed(patName))
                            .thenReturn(pat))
                    .map(pat -> buildAuthentication(pat, token));
        });
    }

    /**
     * Parse an opaque PAT into PAT name and secret.
     *
     * @param token the token without the {@code pat_} prefix, base64url encoded {@code patName:secret}
     * @return a two-element array of PAT name and secret
     */
    private static String[] parseToken(String token) {
        final String decoded;
        try {
            decoded = new String(Base64.getUrlDecoder().decode(token), UTF_8);
        } catch (IllegalArgumentException e) {
            throw new InvalidBearerTokenException("Invalid personal access token.", e);
        }
        var separator = decoded.indexOf(':');
        if (separator <= 0 || separator == decoded.length() - 1) {
            throw new InvalidBearerTokenException("Invalid personal access token.");
        }
        return new String[] {decoded.substring(0, separator), decoded.substring(separator + 1)};
    }

    private Mono<Void> opaquePatChecks(PersonalAccessToken pat, String secret) {
        if (ExtensionUtil.isDeleted(pat)) {
            return Mono.error(new InvalidBearerTokenException("Personal access token is being deleted."));
        }
        var spec = pat.getSpec();
        if (spec.isRevoked()) {
            return Mono.error(new InvalidBearerTokenException("Token has been revoked."));
        }
        var expiresAt = spec.getExpiresAt();
        if (expiresAt != null && expiresAt.isBefore(clock.instant())) {
            return Mono.error(new InvalidBearerTokenException("Token has expired."));
        }
        if (!secretMatches(spec.getTokenId(), secret)) {
            return Mono.error(
                    new InvalidBearerTokenException("Token secret does not match the personal access token."));
        }
        return Mono.empty();
    }

    private static boolean secretMatches(String tokenId, String secret) {
        final HashCode expected;
        try {
            expected = HashCode.fromString(tokenId);
        } catch (IllegalArgumentException e) {
            return false;
        }
        var actual = Hashing.sha256().hashString(secret, UTF_8);
        return MessageDigest.isEqual(expected.asBytes(), actual.asBytes());
    }

    private Authentication buildAuthentication(PersonalAccessToken pat, String tokenValue) {
        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + ANONYMOUS_ROLE_NAME));
        authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + AUTHENTICATED_ROLE_NAME));
        var roles = pat.getSpec().getRoles();
        if (roles != null) {
            roles.stream()
                    .map(role -> AuthorityUtils.ROLE_PREFIX + role)
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }
        var username = pat.getSpec().getUsername();
        // Wrap a synthesized JWT to keep the authentication type consistent with the legacy path.
        var jwt = Jwt.withTokenValue(tokenValue)
                .header("alg", "none")
                .claim("sub", username)
                .claim("pat_name", pat.getMetadata().getName())
                .issuedAt(clock.instant())
                .build();
        return new JwtAuthenticationToken(jwt, authorities, username);
    }

    private Mono<JwtAuthenticationToken> checkAndRebuild(JwtAuthenticationToken jat) {
        var jwt = jat.getToken();
        var patName = jwt.getClaimAsString("pat_name");
        var jwtId = jwt.getClaimAsString(JWTClaimNames.JWT_ID);
        if (patName == null || jwtId == null) {
            // Not a valid PAT
            return Mono.error(new InvalidBearerTokenException("Missing claim pat_name or jti"));
        }
        return client.fetch(PersonalAccessToken.class, patName)
                .switchIfEmpty(Mono.error(() -> new DisabledException("Personal access token has been deleted.")))
                .flatMap(pat -> patChecks(pat, jwtId)
                        .then(checkUser(
                                pat.getSpec().getUsername(), pat.getSpec().getRoles()))
                        .then(updateLastUsed(patName))
                        .thenReturn(pat))
                .map(pat -> {
                    // Make sure the authorities modifiable
                    var authorities = new ArrayList<>(jat.getAuthorities());
                    authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + ANONYMOUS_ROLE_NAME));
                    authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + AUTHENTICATED_ROLE_NAME));
                    var roles = pat.getSpec().getRoles();
                    if (roles != null) {
                        roles.stream()
                                .map(role -> AuthorityUtils.ROLE_PREFIX + role)
                                .map(SimpleGrantedAuthority::new)
                                .forEach(authorities::add);
                    }
                    return new JwtAuthenticationToken(jat.getToken(), authorities, jat.getName());
                });
    }

    private Mono<Void> checkUser(String username, List<String> patRoles) {
        return client.fetch(User.class, username)
                .switchIfEmpty(Mono.error(() -> new InvalidBearerTokenException("User does not exist.")))
                .flatMap(user -> {
                    if (Boolean.TRUE.equals(user.getSpec().getDisabled())) {
                        return Mono.error(new DisabledException("User is disabled"));
                    }
                    if (CollectionUtils.isEmpty(patRoles)) {
                        return Mono.empty();
                    }
                    return roleService
                            .getRolesByUsername(username)
                            .concatWithValues(AUTHENTICATED_ROLE_NAME, ANONYMOUS_ROLE_NAME)
                            .collectList()
                            .flatMap(roles -> roleService.contains(roles, patRoles))
                            .filter(Boolean::booleanValue)
                            .switchIfEmpty(Mono.error(() -> new InvalidBearerTokenException(
                                    "Personal access token roles exceed the user's current roles.")))
                            .then();
                });
    }

    private Mono<Void> updateLastUsed(String patName) {
        // we try our best to update the last used timestamp.

        // the now should be outside the retry cycle because we don't want a fresh timestamp at
        // every retry.
        var now = clock.instant();
        return Mono.defer(
                        // we have to obtain a fresh PAT and retry the update.
                        () -> client.fetch(PersonalAccessToken.class, patName)
                                .filter(pat -> {
                                    var lastUsed = pat.getSpec().getLastUsed();
                                    if (lastUsed == null) {
                                        return true;
                                    }
                                    var diff = Duration.between(lastUsed, now);
                                    return !diff.minus(MIN_UPDATE_GAP).isNegative();
                                })
                                .doOnNext(pat -> pat.getSpec().setLastUsed(now))
                                .flatMap(client::update))
                .retryWhen(Retry.backoff(3, Duration.ofMillis(50))
                        .filter(OptimisticLockingFailureException.class::isInstance))
                .onErrorComplete()
                .then();
    }

    private Mono<Void> patChecks(PersonalAccessToken pat, String tokenId) {
        if (ExtensionUtil.isDeleted(pat)) {
            return Mono.error(new InvalidBearerTokenException("Personal access token is being deleted."));
        }
        var spec = pat.getSpec();
        if (!Objects.equals(spec.getTokenId(), tokenId)) {
            return Mono.error(
                    new InvalidBearerTokenException("Token ID does not match the token ID of personal access token."));
        }
        if (spec.isRevoked()) {
            return Mono.error(new InvalidBearerTokenException("Token has been revoked."));
        }
        return Mono.empty();
    }
}
