package run.halo.app.security.authentication.pat;

import java.util.HashSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.pat.PersonalAccessToken;
import run.halo.app.extension.ReactiveExtensionClient;

@Slf4j
public class PatAuthenticationManager implements ReactiveAuthenticationManager {

    private final PatDecoder decoder;

    private final PatEncoder encoder;

    private final ReactiveExtensionClient client;

    private final ReactiveUserDetailsService userDetailsService;

    public PatAuthenticationManager(PatDecoder decoder, PatEncoder encoder,
                                    ReactiveExtensionClient client,
                                    ReactiveUserDetailsService userDetailsService) {
        this.decoder = decoder;
        this.encoder = encoder;
        this.client = client;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.justOrEmpty(authentication)
            .filter(a -> a instanceof BearerTokenAuthenticationToken)
            .cast(BearerTokenAuthenticationToken.class)
            .map(BearerTokenAuthenticationToken::getToken)
            .flatMap(decoder::decode)
            .flatMap(pat -> client.fetch(PersonalAccessToken.class, pat.getPatName())
                .switchIfEmpty(Mono.error(
                    () -> new InvalidBearerTokenException("Invalid PersonalAccessToken")))
                .filter(personalAccessToken -> encoder.matches(pat.getTokenValue(),
                    personalAccessToken.getSpec().getEncodedToken()))
                .switchIfEmpty(Mono.error(() -> new BadCredentialsException("Invalid Credentials")))
                .doOnNext(personalAccessToken -> this.checkTokenIsValid(pat, personalAccessToken))
                .flatMap(personalAccessToken ->
                    this.createPatAuthenticationToken(pat, personalAccessToken)))
            .onErrorMap(JwtException.class, this::onJwtError);
    }

    private Mono<Authentication> createPatAuthenticationToken(Pat pat,
                                                              PersonalAccessToken personalAccessToken) {
        return userDetailsService.findByUsername(personalAccessToken.getSpec().getCreatedBy())
            .switchIfEmpty(Mono.error(() -> new BadCredentialsException("Token deleted")))
            .map(userDetails -> {
                var authorities =
                    new HashSet<GrantedAuthority>(userDetails.getAuthorities());
                var scopes = personalAccessToken.getSpec().getScopes();
                if (scopes != null) {
                    scopes.stream()
                        .map(scope -> StringUtils.prependIfMissing(scope, "SCOPE_"))
                        .map(ScopeGrantedAuthority::new)
                        .forEach(authorities::add);
                }
                return new PatAuthenticationToken(userDetails, pat.getTokenValue(), authorities);
            });
    }

    private void checkTokenIsValid(Pat pat, PersonalAccessToken personalAccessToken) {
        // validate the personal access token
        var encodedToken = personalAccessToken.getSpec().getEncodedToken();
        if (personalAccessToken.getSpec().isRevoked()) {
            throw new BadCredentialsException("Revoked token");
        }
        if (!encoder.matches(pat.getTokenValue(), encodedToken)) {
            if (log.isDebugEnabled()) {
                log.debug("The token doesn't match the encoded token. Token value: {}",
                    pat.getTokenValue());
            }
            throw new BadCredentialsException("Mismatched token");
        }
    }

    private AuthenticationException onJwtError(JwtException e) {
        if (e instanceof BadJwtException) {
            return new InvalidBearerTokenException(e.getMessage(), e);
        }
        return new AuthenticationServiceException(e.getMessage(), e);
    }
}
