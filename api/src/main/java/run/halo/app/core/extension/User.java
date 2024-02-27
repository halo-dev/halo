package run.halo.app.core.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static run.halo.app.core.extension.User.GROUP;
import static run.halo.app.core.extension.User.KIND;
import static run.halo.app.core.extension.User.VERSION;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * The extension represents user details of Halo.
 *
 * @author johnniang
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = GROUP,
    version = VERSION,
    kind = KIND,
    singular = "user",
    plural = "users")
public class User extends AbstractExtension {

    public static final String GROUP = "";
    public static final String VERSION = "v1alpha1";
    public static final String KIND = "User";

    public static final String USER_RELATED_ROLES_INDEX = "roles";

    public static final String ROLE_NAMES_ANNO = "rbac.authorization.halo.run/role-names";

    public static final String EMAIL_TO_VERIFY = "halo.run/email-to-verify";

    public static final String LAST_AVATAR_ATTACHMENT_NAME_ANNO =
        "halo.run/last-avatar-attachment-name";

    public static final String AVATAR_ATTACHMENT_NAME_ANNO = "halo.run/avatar-attachment-name";

    public static final String HIDDEN_USER_LABEL = "halo.run/hidden-user";

    @Schema(requiredMode = REQUIRED)
    private UserSpec spec = new UserSpec();

    private UserStatus status = new UserStatus();

    @Data
    public static class UserSpec {

        @Schema(requiredMode = REQUIRED)
        private String displayName;

        private String avatar;

        @Schema(requiredMode = REQUIRED)
        private String email;

        private boolean emailVerified;

        private String phone;

        private String password;

        private String bio;

        private Instant registeredAt;

        private Boolean twoFactorAuthEnabled;

        private String totpEncryptedSecret;

        private Boolean disabled;

        private Integer loginHistoryLimit;

    }

    @Data
    public static class UserStatus {

        private Instant lastLoginAt;

        private String permalink;

        private List<LoginHistory> loginHistories;

    }

    @Data
    public static class LoginHistory {

        @Schema(requiredMode = REQUIRED)
        private Instant loginAt;

        @Schema(requiredMode = REQUIRED)
        private String sourceIp;

        @Schema(requiredMode = REQUIRED)
        private String userAgent;

        @Schema(requiredMode = REQUIRED)
        private Boolean successful;

        private String reason;

    }

}
