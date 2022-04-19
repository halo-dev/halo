package run.halo.app.identity.authentication;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.Assert;

/**
 * Base implementation of an {@link Authentication} representing an OAuth 2.0 Authorization Grant.
 *
 * @author guqing
 * @see AbstractAuthenticationToken
 * @see AuthorizationGrantType
 * @see
 * <a href="https://tools.ietf.org/html/rfc6749#section-1.3">Section 1.3 Authorization Grant</a>
 * @since 2.0.0
 */
public class OAuth2AuthorizationGrantAuthenticationToken extends AbstractAuthenticationToken {
    private final AuthorizationGrantType authorizationGrantType;
    private final Map<String, Object> additionalParameters;

    /**
     * Sub-class constructor.
     *
     * @param authorizationGrantType the authorization grant type
     * @param additionalParameters the additional parameters
     */
    protected OAuth2AuthorizationGrantAuthenticationToken(
        AuthorizationGrantType authorizationGrantType,
        @Nullable Map<String, Object> additionalParameters) {
        super(Collections.emptyList());
        Assert.notNull(authorizationGrantType, "authorizationGrantType cannot be null");
        this.authorizationGrantType = authorizationGrantType;
        this.additionalParameters = Collections.unmodifiableMap(
            additionalParameters != null
                ? new HashMap<>(additionalParameters) :
                Collections.emptyMap());
    }

    /**
     * Returns the authorization grant type.
     *
     * @return the authorization grant type
     */
    public AuthorizationGrantType getGrantType() {
        return this.authorizationGrantType;
    }

    @Override
    public Object getPrincipal() {
        return getName();
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    /**
     * Returns the additional parameters.
     *
     * @return the additional parameters
     */
    public Map<String, Object> getAdditionalParameters() {
        return this.additionalParameters;
    }
}
