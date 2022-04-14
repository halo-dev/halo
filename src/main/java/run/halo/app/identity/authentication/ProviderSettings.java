package run.halo.app.identity.authentication;

import java.util.Map;
import org.springframework.util.Assert;
import run.halo.app.infra.config.AbstractSettings;
import run.halo.app.infra.config.ConfigurationSettingNames;

/**
 * A facility for provider configuration settings.
 *
 * @author guqing
 * @date 2022-04-14
 */
public final class ProviderSettings extends AbstractSettings {
    private ProviderSettings(Map<String, Object> settings) {
        super(settings);
    }

    /**
     * Returns the URL of the Provider's Issuer Identifier
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
     * Constructs a new {@link Builder} with the default settings.
     *
     * @return the {@link Builder}
     */
    public static Builder builder() {
        return new Builder()
            .authorizationEndpoint("/oauth2/authorize")
            .tokenEndpoint("/oauth2/token")
            .jwkSetEndpoint("/oauth2/jwks");
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
