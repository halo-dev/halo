package run.halo.app.core.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * Authentication provider extension that describes an external or form-based sign-in integration.
 *
 * @author guqing
 * @since 2.4.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@GVK(
        group = "auth.halo.run",
        version = "v1alpha1",
        kind = "AuthProvider",
        singular = "authprovider",
        plural = "authproviders")
public class AuthProvider extends AbstractExtension {

    public static final String AUTH_BINDING_LABEL = "auth.halo.run/auth-binding";

    public static final String PRIVILEGED_LABEL = "auth.halo.run/privileged";

    /** Desired provider metadata, endpoints, and optional setting references. */
    @Schema(requiredMode = REQUIRED)
    private AuthProviderSpec spec;

    /** Authentication provider metadata and endpoint configuration. */
    @Data
    @ToString
    public static class AuthProviderSpec {

        /** Display name shown on sign-in and account binding screens. */
        @Schema(requiredMode = REQUIRED)
        private String displayName;

        /** Human-readable description of the provider. */
        private String description;

        /** Logo URL or attachment URI for the provider. */
        private String logo;

        /** Provider website URL. */
        private String website;

        /** Help page URL for users who need sign-in assistance. */
        private String helpPage;

        /** URL that starts the authentication flow. */
        @Schema(requiredMode = REQUIRED)
        private String authenticationUrl;

        /** HTTP method used when starting the authentication flow. */
        private String method = "GET";

        /** Whether the provider supports remember-me during sign-in. */
        private boolean rememberMeSupport = false;

        /** Auth type: form or oauth2. */
        @Schema(requiredMode = REQUIRED)
        private AuthType authType = AuthType.OAUTH2;

        /** URL that starts account binding for an already signed-in user. */
        private String bindingUrl;

        /** URL that unbinds the external account from the current user. */
        private String unbindUrl;

        /** Setting definition used to render provider configuration fields. */
        @Schema(requiredMode = NOT_REQUIRED)
        private SettingRef settingRef;

        /** ConfigMap storing provider configuration values. */
        @Schema(requiredMode = NOT_REQUIRED)
        private ConfigMapRef configMapRef;

        public void setAuthType(@Nullable AuthType authType) {
            this.authType = (authType == null ? AuthType.OAUTH2 : authType);
        }
    }

    /** Reference to a setting definition used by this provider. */
    @Data
    @ToString
    public static class SettingRef {
        /** Setting metadata.name. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String name;

        /** Setting form group name. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String group;
    }

    /** Reference to a ConfigMap used by this provider. */
    @Data
    @ToString
    public static class ConfigMapRef {
        /** ConfigMap metadata.name. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String name;
    }

    public enum AuthType {
        FORM,
        OAUTH2
    }
}
