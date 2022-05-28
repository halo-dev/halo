package run.halo.app.identity.apitoken;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Transient;
import run.halo.app.identity.authentication.verifier.AbstractOAuth2TokenAuthenticationToken;

/**
 * An {@link Authentication} implementation used for the Personal Access Token Grant.
 *
 * @author guqing
 * @since 2.0.0
 */
@Transient
public class PersonalAccessTokenAuthenticationToken extends
    AbstractOAuth2TokenAuthenticationToken<PersonalAccessToken> {

    private final String name;

    private final Map<String, Object> tokenAttributes;

    /**
     * Constructs a {@code PersonalAccessToken} using the provided parameters.
     *
     * @param personalAccessToken the PersonalAccessToken
     */
    public PersonalAccessTokenAuthenticationToken(PersonalAccessToken personalAccessToken) {
        this(personalAccessToken, Collections.emptyList(), personalAccessToken.getPrincipalName(),
            Collections.emptyMap());
    }


    /**
     * Constructs a {@code PersonalAccessTokenAuthenticationToken} using the provided parameters.
     *
     * @param personalAccessToken the PersonalAccessToken
     * @param authorities the authorities assigned to the PersonalAccessToken
     */
    public PersonalAccessTokenAuthenticationToken(PersonalAccessToken personalAccessToken,
        Collection<? extends GrantedAuthority> authorities) {
        this(personalAccessToken, authorities, personalAccessToken.getPrincipalName(),
            Collections.emptyMap());
    }

    /**
     * Constructs a {@code PersonalAccessTokenAuthenticationToken} using the provided parameters.
     *
     * @param personalAccessToken the PersonalAccessToken
     * @param authorities the authorities assigned to the PersonalAccessToken
     * @param principleName the principal name
     */
    public PersonalAccessTokenAuthenticationToken(PersonalAccessToken personalAccessToken,
        Collection<? extends GrantedAuthority> authorities,
        String principleName, Map<String, Object> claims) {
        super(personalAccessToken, authorities);
        this.setAuthenticated(true);
        this.name = principleName;
        this.tokenAttributes = claims;
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Map<String, Object> getTokenAttributes() {
        return this.tokenAttributes;
    }

    /**
     * The principal name which is, by default, the {@link PersonalAccessToken}'s tokenValue.
     */
    @Override
    public String getName() {
        return this.name;
    }
}
