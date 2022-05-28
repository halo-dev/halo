package run.halo.app.identity.apitoken;

import java.util.Collection;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import run.halo.app.identity.authentication.verifier.BearerTokenAuthenticationToken;
import run.halo.app.identity.authentication.verifier.InvalidBearerTokenException;

/**
 * <p>An AuthenticationProvider implementation of the personal-access-token based
 * <a href="https://tools.ietf.org/html/rfc6750#section-1.2">Bearer Token</a>
 * s for protecting server resources.</p>
 * <p>This {@link AuthenticationProvider} is responsible for decoding and verifying
 * {@link PersonalAccessTokenUtils#generate(PersonalAccessTokenType, SecretKey)}-generated access
 * token.</p>
 *
 * <p>The composition format of personal-access-token is:
 * <pre>{two letter type prefix}_{32-bit secure random value}{checksum}</pre>
 * Token type prefix is an explicit way to make a token recognizable. such as {@code h}
 * represents {@code halo} and {@code c} represents the content api.</p>
 * <p>Make these prefixes legible in the token to improve readability. Therefore, a separator is
 * added:{@code _} and when you double-click it, it reliably selects the entire token.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class PersonalAccessTokenProvider implements AuthenticationProvider {
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
        PersonalAccessToken accessToken = getPersonalAccessToken(bearer.getToken());

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
        return new PersonalAccessTokenAuthenticationToken(accessToken, authorities,
            accessToken.getPrincipalName(), accessToken.getClaims());
    }

    private PersonalAccessToken getPersonalAccessToken(String tokenValue) {
        try {
            return this.personalAccessTokenDecoder.decode(tokenValue);
        } catch (PersonalAccessTokenException failed) {
            log.debug("Failed to authenticate since the personal-access-token was invalid");
            throw new InvalidBearerTokenException(failed.getMessage(), failed);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PersonalAccessToken.class.isAssignableFrom(authentication);
    }
}
