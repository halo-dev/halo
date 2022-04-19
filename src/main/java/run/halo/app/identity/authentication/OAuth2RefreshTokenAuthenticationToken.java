package run.halo.app.identity.authentication;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.Assert;

/**
 * @author guqing
 * @since 2.0.0
 */
public class OAuth2RefreshTokenAuthenticationToken
    extends OAuth2AuthorizationGrantAuthenticationToken {
    private final String refreshToken;
    private final Set<String> scopes;

    /**
     * Constructs an {@code OAuth2RefreshTokenAuthenticationToken} using the provided parameters.
     *
     * @param refreshToken the refresh token
     * @param scopes the requested scope(s)
     * @param additionalParameters the additional parameters
     */
    public OAuth2RefreshTokenAuthenticationToken(String refreshToken,
        @Nullable Set<String> scopes, @Nullable Map<String, Object> additionalParameters) {
        super(AuthorizationGrantType.REFRESH_TOKEN, additionalParameters);
        Assert.hasText(refreshToken, "refreshToken cannot be empty");
        this.refreshToken = refreshToken;
        this.scopes = Collections.unmodifiableSet(
            scopes != null ? new HashSet<>(scopes) : Collections.emptySet());
    }

    /**
     * Returns the refresh token.
     *
     * @return the refresh token
     */
    public String getRefreshToken() {
        return this.refreshToken;
    }

    /**
     * Returns the requested scope(s).
     *
     * @return the requested scope(s), or an empty {@code Set} if not available
     */
    public Set<String> getScopes() {
        return this.scopes;
    }
}
