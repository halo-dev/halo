package run.halo.app.security.authentication.twofactor;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import org.apache.commons.lang3.StringUtils;
import run.halo.app.core.extension.User;

public enum TwoFactorUtils {
    ;

    public static TwoFactorAuthSettings getTwoFactorAuthSettings(User user) {
        var spec = user.getSpec();
        var tfaEnabled = defaultIfNull(spec.getTwoFactorAuthEnabled(), false);
        var emailVerified = spec.isEmailVerified();
        var totpEncryptedSecret = spec.getTotpEncryptedSecret();
        var settings = new TwoFactorAuthSettings();
        settings.setEnabled(tfaEnabled);
        settings.setEmailVerified(emailVerified);
        settings.setTotpConfigured(StringUtils.isNotBlank(totpEncryptedSecret));
        return settings;
    }

}
