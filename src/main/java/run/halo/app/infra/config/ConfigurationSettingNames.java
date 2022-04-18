package run.halo.app.infra.config;

/**
 * The names for all the configuration settings.
 *
 * @author guqing
 * @date 2022-04-14
 */
public class ConfigurationSettingNames {
    private static final String SETTINGS_NAMESPACE = "settings.";

    private ConfigurationSettingNames() {
    }

    /**
     * The names for provider configuration settings.
     */
    public static final class Provider {
        private static final String PROVIDER_SETTINGS_NAMESPACE =
            SETTINGS_NAMESPACE.concat("provider.");

        /**
         * Set the URL the Provider uses as its Issuer Identifier.
         */
        public static final String ISSUER = PROVIDER_SETTINGS_NAMESPACE.concat("issuer");

        /**
         * Set the Provider's OAuth 2.0 Authorization endpoint.
         */
        public static final String AUTHORIZATION_ENDPOINT =
            PROVIDER_SETTINGS_NAMESPACE.concat("authorization-endpoint");

        /**
         * Set the Provider's OAuth 2.0 Token endpoint.
         */
        public static final String TOKEN_ENDPOINT =
            PROVIDER_SETTINGS_NAMESPACE.concat("token-endpoint");

        /**
         * Set the Provider's JWK Set endpoint.
         */
        public static final String JWK_SET_ENDPOINT =
            PROVIDER_SETTINGS_NAMESPACE.concat("jwk-set-endpoint");

        private Provider() {
        }

    }

    /**
     * The names for token configuration settings.
     */
    public static final class Token {
        private static final String TOKEN_SETTINGS_NAMESPACE = SETTINGS_NAMESPACE.concat("token.");

        /**
         * Set the time-to-live for an access token.
         */
        public static final String ACCESS_TOKEN_TIME_TO_LIVE =
            TOKEN_SETTINGS_NAMESPACE.concat("access-token-time-to-live");

        /**
         * Set to {@code true} if refresh tokens are reused when returning the access token
         * response,
         * or {@code false} if a new refresh token is issued.
         */
        public static final String REUSE_REFRESH_TOKENS =
            TOKEN_SETTINGS_NAMESPACE.concat("reuse-refresh-tokens");

        /**
         * Set the time-to-live for a refresh token.
         */
        public static final String REFRESH_TOKEN_TIME_TO_LIVE =
            TOKEN_SETTINGS_NAMESPACE.concat("refresh-token-time-to-live");

        private Token() {
        }

    }
}
