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
            .flatMap(this::createPatAuthenticationToken)
            .onErrorMap(JwtException.class, this::onJwtError);
    }

    private Mono<Authentication> createPatAuthenticationToken(Pat pat) {
        return client.fetch(PersonalAccessToken.class, pat.getPatName())
            .switchIfEmpty(Mono.error(
                () -> new InvalidBearerTokenException("Invalid PersonalAccessToken")))
            .flatMap(personalAccessToken -> this.checkTokenIsValid(pat, personalAccessToken)
                .thenReturn(personalAccessToken))
            .flatMap(personalAccessToken ->
                this.createPatAuthenticationToken(pat, personalAccessToken));
    }

    private Mono<Authentication> createPatAuthenticationToken(Pat pat,
                                                              PersonalAccessToken accessToken) {
        return userDetailsService.findByUsername(accessToken.getSpec().getCreatedBy())
            .switchIfEmpty(Mono.error(() -> new BadCredentialsException("User Not Found")))
            .map(userDetails -> {
                var authorities = new HashSet<GrantedAuthority>(userDetails.getAuthorities());
                var scopes = accessToken.getSpec().getScopes();
                if (scopes != null) {
                    scopes.stream()
                        .map(scope -> StringUtils.prependIfMissing(scope, "SCOPE_"))
                        .map(ScopeGrantedAuthority::new)
                        .forEach(authorities::add);
                }
                return new PatAuthenticationToken(userDetails, pat.getTokenValue(), authorities);
            });
    }

    private Mono<Void> checkTokenIsValid(Pat pat, PersonalAccessToken personalAccessToken) {
        // validate the personal access token
        if (personalAccessToken.getSpec().isRevoked()) {
            return Mono.error(() -> new BadCredentialsException("Revoked token"));
        }
        var encodedToken = personalAccessToken.getSpec().getEncodedToken();
        return encoder.matches(pat.getTokenValue(), encodedToken)
            .filter(match -> match)
            .switchIfEmpty(Mono.error(() -> {
                if (log.isDebugEnabled()) {
                    log.debug("The token doesn't match the encoded token. Token value: {}",
                        pat.getTokenValue());
                }
                return new BadCredentialsException("Mismatched token");
            }))
            .then();
    }

    private AuthenticationException onJwtError(JwtException e) {
        if (e instanceof BadJwtException) {
            return new InvalidBearerTokenException(e.getMessage(), e);
        }
        return new AuthenticationServiceException(e.getMessage(), e);
    }
}
