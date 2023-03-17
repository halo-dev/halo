package run.halo.app.core.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import java.util.Set;
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

        @Schema(requiredMode = NOT_REQUIRED, defaultValue = "false")
        private Boolean enabled;

        private String helpPage;

        @Schema(requiredMode = REQUIRED, description = "Authentication url of the auth provider")
        private String authenticationUrl;

        private String bindingUrl;

        private String unbindUrl;

        @Schema(requiredMode = REQUIRED)
        private ClientRegistration clientRegistration;

        @Schema(requiredMode = NOT_REQUIRED)
        private SettingRef settingRef;

        @Schema(requiredMode = NOT_REQUIRED)
        private ConfigMapRef configMapRef;
    }

    @Data
    public static class ClientRegistration {

        private String clientAuthenticationMethod;

        private String authorizationGrantType;

        private String redirectUri;

        private Set<String> scopes;

        private String authorizationUri;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String tokenUri;

        private String userInfoUri;

        private String userInfoAuthenticationMethod;

        private String userNameAttributeName;

        private String jwkSetUri;

        private String issuerUri;

        private Map<String, Object> configurationMetadata;

        private String clientName;
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
