package run.halo.app.identity.apitoken;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

/**
 * <p>An implementation of an {@link AbstractOAuth2Token} representing a personal access token.</p>
 * <p>A personal-access-token is a credential that represents an authorization granted by the
 * resource owner.</p>
 * <p>It is primarily used by the client to access protected resources on either a resource
 * server.</p>
 * <p>All personal access tokens created by administrators of the {@code Halo} application are
 * permanent tokens that cannot be regenerated.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
public class PersonalAccessToken extends OAuth2AccessToken {

    public static final AuthorizationGrantType PERSONAL_ACCESS_TOKEN =
        new AuthorizationGrantType("personal_access_token");

    /**
     * Constructs an {@code PersonalAccessToken} using the provided parameters.
     *
     * @param tokenValue the token value
     * @param issuedAt the time at which the token was issued
     */
    public PersonalAccessToken(String tokenValue, Instant issuedAt) {
        this(tokenValue, issuedAt, null);
    }

    /**
     * Constructs an {@code PersonalAccessToken} using the provided parameters.
     *
     * @param tokenValue the token value
     * @param issuedAt the time at which the token was issued
     * @param expiresAt the time at which the token expires
     */
    public PersonalAccessToken(String tokenValue, Instant issuedAt, Instant expiresAt) {
        this(tokenValue, issuedAt, expiresAt, Collections.emptySet());
    }

    /**
     * Constructs an {@code PersonalAccessToken} using the provided parameters.
     *
     * @param tokenValue the token value
     * @param issuedAt the time at which the token was issued
     * @param expiresAt the time at which the token expires
     * @param roles role names
     */
    public PersonalAccessToken(String tokenValue, Instant issuedAt, Instant expiresAt,
        Set<String> roles) {
        super(TokenType.BEARER, tokenValue, issuedAt, expiresAt, roles);
    }
}
