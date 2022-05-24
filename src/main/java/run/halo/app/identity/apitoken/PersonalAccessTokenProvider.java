package run.halo.app.identity.apitoken;

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtException;
import run.halo.app.identity.authentication.verifier.BearerTokenAuthenticationToken;
import run.halo.app.identity.authentication.verifier.InvalidBearerTokenException;

/**
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class PersonalAccessTokenProvider implements AuthenticationProvider {
    private static final String TOKEN_PREFIX = "ha_";
    private final PersonalAccessTokenDecoder personalAccessTokenDecoder;

    public PersonalAccessTokenProvider(PersonalAccessTokenDecoder personalAccessTokenDecoder) {
        this.personalAccessTokenDecoder = personalAccessTokenDecoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        if (!(authentication instanceof BearerTokenAuthenticationToken bearer)) {
            return null;
        }
        String tokenValue = bearer.getToken();
        if (!StringUtils.startsWith(tokenValue, TOKEN_PREFIX)) {
            return null;
        }
        tokenValue = StringUtils.substringAfter(tokenValue, TOKEN_PREFIX);
        PersonalAccessToken accessToken = getPersonalAccessToken(tokenValue);

        PersonalAccessTokenAuthenticationToken token = convert(accessToken);
        token.setDetails(bearer.getDetails());
        log.debug("Authenticated token");
        return token;
    }

    private PersonalAccessTokenAuthenticationToken convert(PersonalAccessToken accessToken) {
        Collection<GrantedAuthority> authorities = accessToken.getScopes()
            .stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
        return new PersonalAccessTokenAuthenticationToken(accessToken, authorities);
    }

    private PersonalAccessToken getPersonalAccessToken(String tokenValue) {
        try {
            return this.personalAccessTokenDecoder.decode(tokenValue);
        } catch (BadJwtException failed) {
            log.debug("Failed to authenticate since the personal-access-token was invalid");
            throw new InvalidBearerTokenException(failed.getMessage(), failed);
        } catch (JwtException failed) {
            throw new AuthenticationServiceException(failed.getMessage(), failed);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PersonalAccessToken.class.isAssignableFrom(authentication);
    }
}
