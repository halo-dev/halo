package run.halo.app.identity.authentication;

import java.time.Duration;
import java.util.Map;
import org.springframework.util.Assert;
import run.halo.app.infra.config.AbstractSettings;
import run.halo.app.infra.config.ConfigurationSettingNames;

/**
 * A facility for provider configuration settings.
 *
 * @author guqing
 * @since 2.0.0
 */
public final class ProviderSettings extends AbstractSettings {
    private ProviderSettings(Map<String, Object> settings) {
        super(settings);
    }

    /**
     * Returns the URL of the Provider's Issuer Identifier.
     *
     * @return the URL of the Provider's Issuer Identifier
     */
    public String getIssuer() {
        return getSetting(ConfigurationSettingNames.Provider.ISSUER);
    }

    /**
     * Returns the Provider's OAuth 2.0 Authorization endpoint. The default is {@code /oauth2
     * /authorize}.
     *
     * @return the Authorization endpoint
     */
    public String getAuthorizationEndpoint() {
        return getSetting(ConfigurationSettingNames.Provider.AUTHORIZATION_ENDPOINT);
    }

    /**
     * Returns the Provider's OAuth 2.0 Token endpoint. The default is {@code /oauth2/token}.
     *
     * @return the Token endpoint
     */
    public String getTokenEndpoint() {
        return getSetting(ConfigurationSettingNames.Provider.TOKEN_ENDPOINT);
    }

    /**
     * Returns the Provider's JWK Set endpoint. The default is {@code /oauth2/jwks}.
     *
     * @return the JWK Set endpoint
     */
    public String getJwkSetEndpoint() {
        return getSetting(ConfigurationSettingNames.Provider.JWK_SET_ENDPOINT);
    }

    /**
     * Returns the time-to-live for an access token. The default is 5 minutes.
     *
     * @return the time-to-live for an access token
     */
    public Duration getAccessTokenTimeToLive() {
        return getSetting(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE);
    }

    /**
     * Returns {@code true} if refresh tokens are reused when returning the access token response,
     * or {@code false} if a new refresh token is issued. The default is {@code true}.
     */
    public boolean isReuseRefreshTokens() {
        return getSetting(ConfigurationSettingNames.Token.REUSE_REFRESH_TOKENS);
    }

    /**
     * Returns the time-to-live for a refresh token. The default is 60 minutes.
     *
     * @return the time-to-live for a refresh token
     */
    public Duration getRefreshTokenTimeToLive() {
        return getSetting(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE);
    }

    /**
     * Constructs a new {@link Builder} with the default settings.
     *
     * @return the {@link Builder}
     */
    public static Builder builder() {
        return new Builder()
            .authorizationEndpoint("/api/v1/oauth2/authorize")
            .tokenEndpoint("/api/v1/oauth2/token")
            .jwkSetEndpoint("/api/v1/oauth2/jwks")
            .accessTokenTimeToLive(Duration.ofMinutes(5L))
            .refreshTokenTimeToLive(Duration.ofMinutes(60))
            .reuseRefreshTokens(false);
    }

    /**
     * Constructs a new {@link Builder} with the provided settings.
     *
     * @param settings the settings to initialize the builder
     * @return the {@link Builder}
     */
    public static Builder withSettings(Map<String, Object> settings) {
        Assert.notEmpty(settings, "settings cannot be empty");
        return new Builder()
            .settings(s -> s.putAll(settings));
    }

    /**
     * A builder for {@link ProviderSettings}.
     */
    public static class Builder extends AbstractBuilder<ProviderSettings, Builder> {

        private Builder() {
        }

        /**
         * Sets the URL the Provider uses as its Issuer Identifier.
         *
         * @param issuer the URL the Provider uses as its Issuer Identifier.
         * @return the {@link Builder} for further configuration
         */
        public Builder issuer(String issuer) {
            return setting(ConfigurationSettingNames.Provider.ISSUER, issuer);
        }

        /**
         * Sets the Provider's OAuth 2.0 Authorization endpoint.
         *
         * @param authorizationEndpoint the Authorization endpoint
         * @return the {@link Builder} for further configuration
         */
        public Builder authorizationEndpoint(String authorizationEndpoint) {
            return setting(ConfigurationSettingNames.Provider.AUTHORIZATION_ENDPOINT,
                authorizationEndpoint);
        }

        /**
         * Sets the Provider's OAuth 2.0 Token endpoint.
         *
         * @param tokenEndpoint the Token endpoint
         * @return the {@link Builder} for further configuration
         */
        public Builder tokenEndpoint(String tokenEndpoint) {
            return setting(ConfigurationSettingNames.Provider.TOKEN_ENDPOINT, tokenEndpoint);
        }

        /**
         * Sets the Provider's JWK Set endpoint.
         *
         * @param jwkSetEndpoint the JWK Set endpoint
         * @return the {@link Builder} for further configuration
         */
        public Builder jwkSetEndpoint(String jwkSetEndpoint) {
            return setting(ConfigurationSettingNames.Provider.JWK_SET_ENDPOINT, jwkSetEndpoint);
        }

        /**
         * Set the time-to-live for an access token. Must be greater than {@code Duration.ZERO}.
         *
         * @param accessTokenTimeToLive the time-to-live for an access token
         * @return the {@link Builder} for further configuration
         */
        public Builder accessTokenTimeToLive(Duration accessTokenTimeToLive) {
            Assert.notNull(accessTokenTimeToLive, "accessTokenTimeToLive cannot be null");
            Assert.isTrue(accessTokenTimeToLive.getSeconds() > 0,
                "accessTokenTimeToLive must be greater than Duration.ZERO");
            return setting(ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE,
                accessTokenTimeToLive);
        }

        /**
         * Set to {@code true} if refresh tokens are reused when returning the access token
         * response,
         * or {@code false} if a new refresh token is issued.
         *
         * @param reuseRefreshTokens {@code true} to reuse refresh tokens, {@code false} to issue
         * new refresh tokens
         * @return the {@link Builder} for further configuration
         */
        public Builder reuseRefreshTokens(boolean reuseRefreshTokens) {
            return setting(ConfigurationSettingNames.Token.REUSE_REFRESH_TOKENS,
                reuseRefreshTokens);
        }

        /**
         * Set the time-to-live for a refresh token. Must be greater than {@code Duration.ZERO}.
         *
         * @param refreshTokenTimeToLive the time-to-live for a refresh token
         * @return the {@link Builder} for further configuration
         */
        public Builder refreshTokenTimeToLive(Duration refreshTokenTimeToLive) {
            Assert.notNull(refreshTokenTimeToLive, "refreshTokenTimeToLive cannot be null");
            Assert.isTrue(refreshTokenTimeToLive.getSeconds() > 0,
                "refreshTokenTimeToLive must be greater than Duration.ZERO");
            return setting(ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE,
                refreshTokenTimeToLive);
        }

        /**
         * Builds the {@link ProviderSettings}.
         *
         * @return the {@link ProviderSettings}
         */
        @Override
        public ProviderSettings build() {
            return new ProviderSettings(getSettings());
        }
    }
}
