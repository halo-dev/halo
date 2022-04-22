package run.halo.app.identity.authentication.verifier;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.Assert;

/**
 * A jwt resolver for {@link AuthenticationManager} use {@link JwtDecoder}.
 *
 * @author guqing
 * @since 2.0.0
 */
public record JwtProvidedDecoderAuthenticationManagerResolver(JwtDecoder jwtDecoder)
    implements AuthenticationManagerResolver<HttpServletRequest> {
    public JwtProvidedDecoderAuthenticationManagerResolver {
        Assert.notNull(jwtDecoder, "jwtDecoder cannot be null");
    }

    @Override
    public AuthenticationManager resolve(HttpServletRequest request) {
        return new JwtAuthenticationProvider(jwtDecoder)::authenticate;
    }
}
