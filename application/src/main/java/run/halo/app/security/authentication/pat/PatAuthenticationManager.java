package run.halo.app.security.authentication.pat;

import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder.withJwkSource;
import static run.halo.app.security.authentication.pat.PatServerWebExchangeMatcher.PAT_TOKEN_PREFIX;
import static run.halo.app.security.authorization.AuthorityUtils.ANONYMOUS_ROLE_NAME;
import static run.halo.app.security.authorization.AuthorityUtils.AUTHENTICATED_ROLE_NAME;
import static run.halo.app.security.authorization.AuthorityUtils.ROLE_PREFIX;

import com.nimbusds.jwt.JWTClaimNames;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Objects;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.security.PersonalAccessToken;
import run.halo.app.security.authentication.CryptoService;
import run.halo.app.security.authorization.AuthorityUtils;

public class PatAuthenticationManager implements ReactiveAuthenticationManager {

    /**
     * Minimal duration gap of personal access token update.
     */
    private static final Duration MIN_UPDATE_GAP = Duration.ofMinutes(1);

    private final ReactiveAuthenticationManager delegate;

    private final ReactiveExtensionClient client;

    private final CryptoService cryptoService;

    private Clock clock;

    public PatAuthenticationManager(ReactiveExtensionClient client, CryptoService cryptoService) {
        this.client = client;
        this.cryptoService = cryptoService;
        this.delegate = getDelegate();
        this.clock = Clock.systemDefaultZone();
    }

    private ReactiveAuthenticationManager getDelegate() {
        var jwtDecoder = withJwkSource(signedJWT -> Flux.just(cryptoService.getJwk()))
            .build();
        return new JwtReactiveAuthenticationManager(jwtDecoder);
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.just(authentication)
            .map(this::clearPrefix)
            .flatMap(delegate::authenticate)
            .cast(JwtAuthenticationToken.class)
            .flatMap(this::checkAndRebuild);
    }

    private Authentication clearPrefix(Authentication authentication) {
        if (authentication instanceof BearerTokenAuthenticationToken bearerToken) {
            var newToken = removeStart(bearerToken.getToken(), PAT_TOKEN_PREFIX);
            return new BearerTokenAuthenticationToken(newToken);
        }
        return authentication;
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
            .switchIfEmpty(
                Mono.error(() -> new DisabledException("Personal access token has been deleted."))
            )
            .flatMap(pat -> patChecks(pat, jwtId).and(updateLastUsed(patName)).thenReturn(pat))
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
                    .flatMap(client::update)
            )
            .retryWhen(Retry.backoff(3, Duration.ofMillis(50))
                .filter(OptimisticLockingFailureException.class::isInstance))
            .onErrorComplete()
            .then();
    }

    private Mono<Void> patChecks(PersonalAccessToken pat, String tokenId) {
        if (ExtensionUtil.isDeleted(pat)) {
            return Mono.error(
                new InvalidBearerTokenException("Personal access token is being deleted."));
        }
        var spec = pat.getSpec();
        if (!Objects.equals(spec.getTokenId(), tokenId)) {
            return Mono.error(new InvalidBearerTokenException(
                "Token ID does not match the token ID of personal access token."));
        }
        if (spec.isRevoked()) {
            return Mono.error(new InvalidBearerTokenException("Token has been revoked."));
        }
        return Mono.empty();
    }
}
