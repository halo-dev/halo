package run.halo.app.core.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static run.halo.app.core.extension.User.GROUP;
import static run.halo.app.core.extension.User.KIND;
import static run.halo.app.core.extension.User.VERSION;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * User extension that stores profile, authentication, and account state for a Halo user.
 *
 * @author johnniang
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = GROUP, version = VERSION, kind = KIND, singular = "user", plural = "users")
public class User extends AbstractExtension {

    public static final String GROUP = "";
    public static final String VERSION = "v1alpha1";
    public static final String KIND = "User";

    public static final String USER_RELATED_ROLES_INDEX = "roles";

    public static final String ROLE_NAMES_ANNO = "rbac.authorization.halo.run/role-names";

    public static final String EMAIL_TO_VERIFY = "halo.run/email-to-verify";

    public static final String LAST_AVATAR_ATTACHMENT_NAME_ANNO = "halo.run/last-avatar-attachment-name";

    public static final String AVATAR_ATTACHMENT_NAME_ANNO = "halo.run/avatar-attachment-name";

    public static final String HIDDEN_USER_LABEL = "halo.run/hidden-user";

    public static final String REQUEST_TO_UPDATE = "halo.run/request-to-update";

    /** Desired user profile and authentication settings. */
    @Schema(requiredMode = REQUIRED)
    private UserSpec spec = new UserSpec();

    /** Observed user state derived by the application. */
    private UserStatus status = new UserStatus();

    /** Desired user profile and authentication settings. */
    @Data
    public static class UserSpec {

        /** Display name shown for the user. */
        @Schema(requiredMode = REQUIRED)
        private String displayName;

        /** Avatar URL or attachment URI for the user. */
        private String avatar;

        /** Email address used for sign-in and notifications. */
        @Schema(requiredMode = REQUIRED)
        private String email;

        /** Whether the email address has been verified. */
        private boolean emailVerified;

        /** Phone number associated with the user. */
        private String phone;

        /** Password hash or encoded password value. */
        private String password;

        /** User biography shown on profile surfaces. */
        private String bio;

        /** Time when the user registered. */
        private Instant registeredAt;

        /** Whether two-factor authentication is enabled for this user. */
        private Boolean twoFactorAuthEnabled;

        /** Encrypted TOTP secret used for two-factor authentication. */
        private String totpEncryptedSecret;

        /** Whether the user account is disabled. */
        private Boolean disabled;

        /** Maximum number of login history entries retained for this user. */
        private Integer loginHistoryLimit;
    }

    /** Observed user state derived by the application. */
    @Data
    public static class UserStatus {

        /** Public permalink of the user's profile or author page. */
        private String permalink;
    }
}
