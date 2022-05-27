package run.halo.app.identity.authentication.verifier;

import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.http.HttpServletRequest;
import java.text.ParseException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.Assert;
import run.halo.app.identity.apitoken.PersonalAccessTokenDecoder;
import run.halo.app.identity.apitoken.PersonalAccessTokenProvider;
import run.halo.app.identity.apitoken.PersonalTokenTypeUtils;

/**
 * A token resolver for {@link AuthenticationManager} use {@link JwtDecoder} and
 * {@link PersonalAccessTokenDecoder}.
 *
 * @author guqing
 * @since 2.0.0
 */
public record TokenAuthenticationManagerResolver(JwtDecoder jwtDecoder,
                                                 PersonalAccessTokenDecoder personalTokenDecoder)
    implements AuthenticationManagerResolver<HttpServletRequest> {

    private static final DefaultBearerTokenResolver defaultBearerTokenResolver =
        new DefaultBearerTokenResolver();

    public TokenAuthenticationManagerResolver {
        Assert.notNull(jwtDecoder, "jwtDecoder cannot be null");
        Assert.notNull(personalTokenDecoder, "personalAccessTokenDecoder cannot be null");
    }

    @Override
    public AuthenticationManager resolve(HttpServletRequest request) {

        String bearerToken = defaultBearerTokenResolver.resolve(request);

        if (useJwt(bearerToken)) {
            return new JwtAuthenticationProvider(jwtDecoder)::authenticate;
        } else if (PersonalTokenTypeUtils.isPersonalAccessToken(bearerToken)) {
            return new PersonalAccessTokenProvider(personalTokenDecoder)::authenticate;
        }

        return authentication -> {
            throw new AuthenticationServiceException("Cannot authenticate " + authentication);
        };
    }

    private boolean useJwt(String token) {
        try {
            JWTParser.parse(token);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
