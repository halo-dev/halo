package run.halo.app.identity.authentication.verifier;

import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.http.HttpServletRequest;
import java.text.ParseException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.Assert;
import run.halo.app.identity.apitoken.PersonalAccessTokenDecoder;
import run.halo.app.identity.apitoken.PersonalAccessTokenProvider;

/**
 * A jwt resolver for {@link AuthenticationManager} use {@link JwtDecoder}.
 *
 * @author guqing
 * @since 2.0.0
 */
public record JwtProvidedDecoderAuthenticationManagerResolver(JwtDecoder jwtDecoder, PersonalAccessTokenDecoder personalAccessTokenDecoder)
    implements AuthenticationManagerResolver<HttpServletRequest> {

    public JwtProvidedDecoderAuthenticationManagerResolver {
        Assert.notNull(jwtDecoder, "jwtDecoder cannot be null");
    }

    @Override
    public AuthenticationManager resolve(HttpServletRequest request) {
        DefaultBearerTokenResolver defaultBearerTokenResolver = new DefaultBearerTokenResolver();
        String bearerToken = defaultBearerTokenResolver.resolve(request);
        if (isJwt(bearerToken)) {
            return new JwtAuthenticationProvider(jwtDecoder)::authenticate;
        } else {
            return new PersonalAccessTokenProvider(personalAccessTokenDecoder)::authenticate;
        }
    }

    private boolean isJwt(String token) {
        try {
            JWTParser.parse(token);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
