package run.halo.app.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface HaloUserDetails extends UserDetails {

    /**
     * Checks if two-factor authentication is enabled.
     *
     * @return true if two-factor authentication is enabled, false otherwise.
     */
    boolean isTwoFactorAuthEnabled();

    /**
     * Gets the encrypted secret of TOTP.
     *
     * @return encrypted secret of TOTP.
     */
    String getTotpEncryptedSecret();

}
