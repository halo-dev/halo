package run.halo.app.identity.authentication.verifier;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Transient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.util.Assert;

/**
 * An {@link org.springframework.security.core.Authentication} token that represents a
 * successful authentication as obtained through a bearer token.
 *
 * @author guqing
 * @since 2.0.0
 */
@Transient
public class BearerTokenAuthentication
    extends AbstractOAuth2TokenAuthenticationToken<OAuth2AccessToken> {
    private final Map<String, Object> attributes;

    /**
     * Constructs a {@link BearerTokenAuthentication} with the provided arguments.
     *
     * @param principal The OAuth 2.0 attributes
     * @param credentials The verified token
     * @param authorities The authorities associated with the given token
     */
    public BearerTokenAuthentication(OAuth2AuthenticatedPrincipal principal,
        OAuth2AccessToken credentials,
        Collection<? extends GrantedAuthority> authorities) {
        super(credentials, principal, credentials, authorities);
        Assert.isTrue(credentials.getTokenType() == OAuth2AccessToken.TokenType.BEARER,
            "credentials must be a bearer token");
        this.attributes =
            Collections.unmodifiableMap(new LinkedHashMap<>(principal.getAttributes()));
        setAuthenticated(true);
    }

    @Override
    public Map<String, Object> getTokenAttributes() {
        return this.attributes;
    }
}
