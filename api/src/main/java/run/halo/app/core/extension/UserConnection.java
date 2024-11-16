package run.halo.app.core.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Metadata;

/**
 * User connection extension.
 *
 * @author guqing
 * @since 2.4.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "auth.halo.run", version = "v1alpha1", kind = "UserConnection",
    singular = "userconnection", plural = "userconnections")
public class UserConnection extends AbstractExtension {

    @Schema(requiredMode = REQUIRED)
    private UserConnectionSpec spec;

    @Data
    public static class UserConnectionSpec {

        /**
         * The name of the OAuth provider (e.g. Google, Facebook, Twitter).
         */
        @Schema(requiredMode = REQUIRED)
        private String registrationId;

        /**
         * The {@link Metadata#getName()} of the user associated with the OAuth connection.
         */
        @Schema(requiredMode = REQUIRED)
        private String username;

        /**
         * The unique identifier for the user's connection to the OAuth provider.
         * for example, the user's GitHub id.
         */
        @Schema(requiredMode = REQUIRED)
        private String providerUserId;

        /**
         * The time when the user connection was last updated.
         */
        private Instant updatedAt;

    }
}
