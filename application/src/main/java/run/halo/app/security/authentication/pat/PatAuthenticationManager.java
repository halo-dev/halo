package run.halo.app.security.authentication.pat;

import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder.withJwkSource;
import static run.halo.app.security.authentication.pat.PatServerWebExchangeMatcher.PAT_TOKEN_PREFIX;

import com.nimbusds.jwt.JWTClaimNames;
import java.time.Clock;
import java.util.Objects;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.security.PersonalAccessToken;
import run.halo.app.security.authentication.jwt.JwtScopesAndRolesGrantedAuthoritiesConverter;

public class PatAuthenticationManager implements ReactiveAuthenticationManager {

    private final ReactiveAuthenticationManager delegate;

    private final ReactiveExtensionClient client;

    private Clock clock;

    public PatAuthenticationManager(ReactiveExtensionClient client, PatJwkSupplier jwkSupplier) {
        this.client = client;
        this.delegate = getDelegate(jwkSupplier);
        this.clock = Clock.systemDefaultZone();
    }

    private ReactiveAuthenticationManager getDelegate(PatJwkSupplier jwkSupplier) {
        var jwtDecoder = withJwkSource(signedJWT -> Flux.just(jwkSupplier.getJwk()))
            .build();
        var jwtAuthManager = new JwtReactiveAuthenticationManager(jwtDecoder);
        var jwtAuthConverter = new ReactiveJwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(
            new JwtScopesAndRolesGrantedAuthoritiesConverter());
        jwtAuthManager.setJwtAuthenticationConverter(jwtAuthConverter);
        return jwtAuthManager;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return delegate.authenticate(clearPrefix(authentication))
            .transformDeferred(auth -> auth.filter(a -> a instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .flatMap(jwtAuthToken -> checkAvailability(jwtAuthToken).thenReturn(jwtAuthToken)));
    }

    private Authentication clearPrefix(Authentication authentication) {
        if (authentication instanceof BearerTokenAuthenticationToken bearerToken) {
            var newToken = removeStart(bearerToken.getToken(), PAT_TOKEN_PREFIX);
            return new BearerTokenAuthenticationToken(newToken);
        }
        return authentication;
    }

    private Mono<Void> checkAvailability(JwtAuthenticationToken jwtAuthToken) {
        var jwt = jwtAuthToken.getToken();
        var patName = jwt.getClaimAsString("pat_name");
        var jwtId = jwt.getClaimAsString(JWTClaimNames.JWT_ID);
        if (patName == null || jwtId == null) {
            // Skip if the JWT token is not a PAT.
            return Mono.empty();
        }
        return client.fetch(PersonalAccessToken.class, patName)
            .switchIfEmpty(
                Mono.error(() -> new DisabledException("Personal access token has been deleted.")))
            .flatMap(pat -> patChecks(pat, jwtId)
                .then(updateLastUsed(pat))
                .then()
            );
    }

    private Mono<PersonalAccessToken> updateLastUsed(PersonalAccessToken pat) {
        return Mono.defer(() -> {
            pat.getSpec().setLastUsed(clock.instant());
            return client.update(pat);
        });
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
