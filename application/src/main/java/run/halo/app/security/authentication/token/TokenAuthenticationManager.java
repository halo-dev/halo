package run.halo.app.security.authentication.token;

import static run.halo.app.security.PersonalAccessToken.PAT_TOKEN_PREFIX;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.security.authorization.AuthorityUtils;

/**
 * Authentication manager for bearer token based authorization.
 *
 * @author johnniang
 * @since 2.24.0
 */
class TokenAuthenticationManager implements ReactiveAuthenticationManager {

    private final ReactiveExtensionClient client;

    private final ReactiveJwtDecoder jwtDecoder;

    private final ReactiveAuthenticationManager patAuthManager;

    private final ReactiveAuthenticationManager jwtAuthManager;

    TokenAuthenticationManager(ReactiveExtensionClient client, ReactiveJwtDecoder jwtDecoder) {
        this.client = client;
        this.jwtDecoder = jwtDecoder;
        this.patAuthManager = createPatAuthManager();
        this.jwtAuthManager = createJwtAuthManager();
    }

    @Override
    public Mono<Authentication> authenticate(Authentication a) {
        if (isPatToken(a)) {
            return patAuthManager.authenticate(a);
        }
        return jwtAuthManager.authenticate(a);
    }

    private static boolean isPatToken(Authentication a) {
        return a instanceof BearerTokenAuthenticationToken t
                && t.getToken().startsWith(PAT_TOKEN_PREFIX);
    }

    private ReactiveAuthenticationManager createPatAuthManager() {
        var delegate = new JwtReactiveAuthenticationManager(this.jwtDecoder);
        return new PatAuthenticationManager(client, delegate);
    }

    private ReactiveAuthenticationManager createJwtAuthManager() {
        var authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix(AuthorityUtils.ROLE_PREFIX);

        var converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(
                new ReactiveJwtGrantedAuthoritiesConverterAdapter(authoritiesConverter));

        var authManager = new JwtReactiveAuthenticationManager(this.jwtDecoder);
        authManager.setJwtAuthenticationConverter(new NonPatJwtAuthenticationConverter(converter));
        return authManager;
    }
}
