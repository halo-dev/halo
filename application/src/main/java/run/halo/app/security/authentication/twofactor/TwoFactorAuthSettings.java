package run.halo.app.security.authentication.twofactor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** Two-factor authentication settings for the current user. */
@Data
public class TwoFactorAuthSettings {

    /** Whether two-factor authentication is enabled for the user. */
    @Schema(requiredMode = REQUIRED)
    private boolean enabled;

    /** Whether the user's email address has been verified. */
    @Schema(requiredMode = REQUIRED)
    private boolean emailVerified;

    /** Whether a TOTP authenticator has been configured. */
    @Schema(requiredMode = REQUIRED)
    private boolean totpConfigured;

    /**
     * Check if 2FA is available.
     *
     * @return true if 2FA is enabled and configured, false otherwise.
     */
    @Schema(requiredMode = REQUIRED)
    public boolean isAvailable() {
        return enabled && totpConfigured;
    }
}
