package run.halo.app.identity.authentication.verifier;

import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.util.Assert;

/**
 * An {@link AuthenticationProvider} implementation of the {@link Jwt}-encoded
 * <a href="https://tools.ietf.org/html/rfc6750#section-1.2">Bearer Token</a>
 * s for protecting server resources.
 * <p>
 * <p>
 * This {@link AuthenticationProvider} is responsible for decoding and verifying a
 * {@link Jwt}-encoded access token, returning its claims set as part of the
 * {@link Authentication} statement.
 * <p>
 * <p>
 * Scopes are translated into {@link GrantedAuthority}s according to the following
 * algorithm:
 * <p>
 * 1. If there is a "scope" or "scp" attribute, then if a {@link String}, then split by
 * spaces and return, or if a {@link Collection}, then simply return 2. Take the resulting
 * {@link Collection} of {@link String}s and prepend the "SCOPE_" keyword, adding as
 * {@link GrantedAuthority}s.
 *
 * @author guqing
 * @see AuthenticationProvider
 * @see JwtDecoder
 * @since 2.0.0
 */
@Slf4j
public class JwtAuthenticationProvider implements AuthenticationProvider {
    private final JwtDecoder jwtDecoder;

    private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter =
        new JwtAuthenticationConverter();

    public JwtAuthenticationProvider(JwtDecoder jwtDecoder) {
        Assert.notNull(jwtDecoder, "jwtDecoder cannot be null");
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Decode and validate the
     * <a href="https://tools.ietf.org/html/rfc6750#section-1.2">Bearer Token</a>.
     *
     * @param authentication the authentication request object.
     * @return A successful authentication
     * @throws AuthenticationException if authentication failed for some reason
     */
    @Override
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;
        Jwt jwt = getJwt(bearer);
        AbstractAuthenticationToken token = this.jwtAuthenticationConverter.convert(jwt);
        if (token != null) {
            token.setDetails(bearer.getDetails());
        }
        log.debug("Authenticated token");
        return token;
    }

    private Jwt getJwt(BearerTokenAuthenticationToken bearer) {
        try {
            return this.jwtDecoder.decode(bearer.getToken());
        } catch (BadJwtException failed) {
            log.debug("Failed to authenticate since the JWT was invalid");
            throw new InvalidBearerTokenException(failed.getMessage(), failed);
        } catch (JwtException failed) {
            throw new AuthenticationServiceException(failed.getMessage(), failed);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public void setJwtAuthenticationConverter(
        Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter) {
        Assert.notNull(jwtAuthenticationConverter, "jwtAuthenticationConverter cannot be null");
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }
}
