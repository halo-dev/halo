package run.halo.app.identity.apitoken;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.util.Assert;

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

    private final String principalName;

    private final Map<String, Object> claims;

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
        this(tokenValue, issuedAt, expiresAt, Set.of(), "", Map.of());
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
        Set<String> roles, String principalName, Map<String, Object> attributes) {
        super(TokenType.BEARER, tokenValue, issuedAt, expiresAt, roles);
        this.principalName = principalName;
        this.claims = attributes;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public Map<String, Object> getClaims() {
        return claims;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements Serializable {
        private String principalName;

        private Map<String, Object> attributes;

        private Set<String> scopes;

        private String tokenValue;

        private Instant issuedAt;

        private Instant expiresAt;

        public Builder principalName(String principalName) {
            this.principalName = principalName;
            return this;
        }

        public Builder scopes(Set<String> scopes) {
            this.scopes = Objects.requireNonNullElse(scopes, Set.of());
            return this;
        }

        public Builder tokenValue(String tokenValue) {
            this.tokenValue = tokenValue;
            return this;
        }

        public Builder issuedAt(Instant issuedAt) {
            this.issuedAt = issuedAt;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes = Objects.requireNonNullElse(attributes, Map.of());
            return this;
        }

        /**
         * Populate tokenValue, issuedAt, expiresAt, scopes attributes from
         * {@link OAuth2AccessToken}.
         *
         * @param accessToken oauth2 access token
         * @return builder
         */
        public Builder token(OAuth2AccessToken accessToken) {
            Assert.notNull(accessToken, "The accessToken must not be null.");
            this.tokenValue = accessToken.getTokenValue();
            this.issuedAt = accessToken.getIssuedAt();
            this.expiresAt = accessToken.getExpiresAt();
            this.scopes = accessToken.getScopes();
            return this;
        }

        public PersonalAccessToken build() {
            return new PersonalAccessToken(tokenValue, issuedAt, expiresAt,
                scopes, principalName, attributes);
        }
    }
}
