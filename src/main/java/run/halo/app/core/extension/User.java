package run.halo.app.core.extension;

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
@GVK(group = "", version = "v1alpha1", kind = "User",
    singular = "user", plural = "users")
public class User extends AbstractExtension {

    private UserSpec spec;

    private UserStatus status;

    @Data
    public static class UserSpec {

        private String displayName;

        private String avatar;

        private String email;

        private String phone;

        private String password;

        private String bio;

        private Instant registeredAt;

        private Boolean twoFactorAuthEnabled;

        private Boolean disabled;

        private Integer loginHistoryLimit;

    }

    @Data
    public static class UserStatus {

        private Instant lastLoginAt;

        private List<LoginHistory> loginHistories;

    }

    @Data
    public static class LoginHistory {

        private Instant loginAt;

        private String sourceIp;

        private String userAgent;

        private Boolean successful;

        private String reason;

    }

}
