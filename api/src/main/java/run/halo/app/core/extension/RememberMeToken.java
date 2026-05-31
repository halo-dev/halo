package run.halo.app.core.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/** Persistent remember-me token used to keep a user's browser session signed in across restarts. */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(
        group = "security.halo.run",
        version = "v1alpha1",
        kind = "RememberMeToken",
        plural = "remembermetokens",
        singular = "remembermetoken")
public class RememberMeToken extends AbstractExtension {

    /** Desired token identity and rotation state. */
    @Schema(requiredMode = REQUIRED)
    private Spec spec;

    /** Persistent remember-me token values for a user and browser series. */
    @Data
    @Accessors(chain = true)
    @Schema(name = "RememberMeTokenSpec")
    public static class Spec {
        /** User metadata.name that owns this remember-me token. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String username;

        /** Browser series identifier used by Spring Security remember-me authentication. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String series;

        /** Current remember-me token value expected from the browser cookie. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String tokenValue;

        /** Last time this remember-me token was used successfully. */
        @Nullable
        private Instant lastUsed;

        /**
         * The previous token value, stored when the token is rotated. Used to accept recently-rotated tokens within a
         * grace period to prevent false-positive cookie theft detection during concurrent requests.
         */
        @Nullable
        private String previousTokenValue;
    }
}
