package run.halo.app.identity.authentication;

import java.util.Collections;
import java.util.Map;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.util.Assert;

/**
 * @author guqing
 * @since 2.0
 */
public class OAuth2AccessTokenAuthenticationToken extends AbstractAuthenticationToken {
    private final Authentication principal;
    private final OAuth2AccessToken accessToken;
    private final OAuth2RefreshToken refreshToken;
    private final Map<String, Object> additionalParameters;

    /**
     * Constructs an {@code OAuth2AccessTokenAuthenticationToken} using the provided parameters.
     *
     * @param clientPrincipal the authenticated client principal
     * @param accessToken the access token
     */
    public OAuth2AccessTokenAuthenticationToken(Authentication clientPrincipal,
        OAuth2AccessToken accessToken) {
        this(clientPrincipal, accessToken, null);
    }

    /**
     * Constructs an {@code OAuth2AccessTokenAuthenticationToken} using the provided parameters.
     *
     * @param clientPrincipal the authenticated client principal
     * @param accessToken the access token
     * @param refreshToken the refresh token
     */
    public OAuth2AccessTokenAuthenticationToken(Authentication clientPrincipal,
        OAuth2AccessToken accessToken, @Nullable OAuth2RefreshToken refreshToken) {
        this(clientPrincipal, accessToken, refreshToken, Collections.emptyMap());
    }

    /**
     * Constructs an {@code OAuth2AccessTokenAuthenticationToken} using the provided parameters.
     *
     * @param principal the authenticated principal
     * @param accessToken the access token
     * @param refreshToken the refresh token
     * @param additionalParameters the additional parameters
     */
    public OAuth2AccessTokenAuthenticationToken(Authentication principal,
        OAuth2AccessToken accessToken, @Nullable OAuth2RefreshToken refreshToken,
        Map<String, Object> additionalParameters) {
        super(Collections.emptyList());
        Assert.notNull(principal, "principal cannot be null");
        Assert.notNull(accessToken, "accessToken cannot be null");
        Assert.notNull(additionalParameters, "additionalParameters cannot be null");
        this.principal = principal;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.additionalParameters = additionalParameters;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    /**
     * Returns the {@link OAuth2AccessToken access token}.
     *
     * @return the {@link OAuth2AccessToken}
     */
    public OAuth2AccessToken getAccessToken() {
        return this.accessToken;
    }

    /**
     * Returns the {@link OAuth2RefreshToken refresh token}.
     *
     * @return the {@link OAuth2RefreshToken} or {@code null} if not available
     */
    @Nullable
    public OAuth2RefreshToken getRefreshToken() {
        return this.refreshToken;
    }

    /**
     * Returns the additional parameters.
     *
     * @return a {@code Map} of the additional parameters, may be empty
     */
    public Map<String, Object> getAdditionalParameters() {
        return this.additionalParameters;
    }
}
