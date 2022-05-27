package run.halo.app.identity.apitoken;

import java.util.Collection;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Transient;
import run.halo.app.identity.authentication.verifier.AbstractOAuth2TokenAuthenticationToken;

/**
 * @author guqing
 * @since 2.0.0
 */
@Transient
public class PersonalAccessTokenAuthenticationToken extends
    AbstractOAuth2TokenAuthenticationToken<PersonalAccessToken> {

    private final String name;

    /**
     * Constructs a {@code PersonalAccessToken} using the provided parameters.
     *
     * @param personalAccessToken the PersonalAccessToken
     */
    public PersonalAccessTokenAuthenticationToken(PersonalAccessToken personalAccessToken) {
        super(personalAccessToken);
        this.name = personalAccessToken.getTokenValue();
    }


    /**
     * Constructs a {@code PersonalAccessTokenAuthenticationToken} using the provided parameters.
     *
     * @param personalAccessToken the PersonalAccessToken
     * @param authorities the authorities assigned to the PersonalAccessToken
     */
    public PersonalAccessTokenAuthenticationToken(PersonalAccessToken personalAccessToken,
        Collection<? extends GrantedAuthority> authorities) {
        super(personalAccessToken, authorities);
        this.setAuthenticated(true);
        this.name = personalAccessToken.getTokenValue();
    }

    /**
     * Constructs a {@code PersonalAccessTokenAuthenticationToken} using the provided parameters.
     *
     * @param personalAccessToken the PersonalAccessToken
     * @param authorities the authorities assigned to the PersonalAccessToken
     * @param name the principal name
     */
    public PersonalAccessTokenAuthenticationToken(PersonalAccessToken personalAccessToken,
        Collection<? extends GrantedAuthority> authorities,
        String name) {
        super(personalAccessToken, authorities);
        this.setAuthenticated(true);
        this.name = name;
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Map<String, Object> getTokenAttributes() {
        return Map.of();
    }

    /**
     * The principal name which is, by default, the {@link PersonalAccessToken}'s tokenValue.
     */
    @Override
    public String getName() {
        return this.name;
    }
}
