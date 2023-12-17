package run.halo.app.core.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * Auth provider extension.
 *
 * @author guqing
 * @since 2.4.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@GVK(group = "auth.halo.run", version = "v1alpha1", kind = "AuthProvider",
    singular = "authprovider", plural = "authproviders")
public class AuthProvider extends AbstractExtension {

    public static final String AUTH_BINDING_LABEL = "auth.halo.run/auth-binding";
    
    public static final String PRIVILEGED_LABEL = "auth.halo.run/privileged";

    @Schema(requiredMode = REQUIRED)
    private AuthProviderSpec spec;

    @Data
    @ToString
    public static class AuthProviderSpec {

        @Schema(requiredMode = REQUIRED, description = "Display name of the auth provider")
        private String displayName;

        private String description;

        private String logo;

        private String website;

        private String helpPage;

        @Schema(requiredMode = REQUIRED, description = "Authentication url of the auth provider")
        private String authenticationUrl;

        private String bindingUrl;

        private String unbindUrl;

        @Schema(requiredMode = NOT_REQUIRED)
        private SettingRef settingRef;

        @Schema(requiredMode = NOT_REQUIRED)
        private ConfigMapRef configMapRef;
    }

    @Data
    @ToString
    public static class SettingRef {
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String name;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String group;
    }

    @Data
    @ToString
    public static class ConfigMapRef {
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String name;
    }
}
