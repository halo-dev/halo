package run.halo.app.security.authentication.twofactor;

import lombok.Data;

@Data
public class TwoFactorAuthSettings {

    private boolean enabled;

    private boolean emailVerified;

    private boolean totpConfigured;

    /**
     * Check if 2FA is available.
     *
     * @return true if 2FA is enabled and configured, false otherwise.
     */
    public boolean isAvailable() {
        return enabled && totpConfigured;
    }
}
