package run.halo.app.security.authentication.twofactor;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.security.authentication.twofactor.totp.TotpAuthService;

/** TOTP verification for a user. Shared by 2FA settings endpoints and security verification. */
@Component
@RequiredArgsConstructor
public class TotpVerificationService {

    private final TotpAuthService totpAuthService;

    /** Validate the TOTP code of the given user. Passes when TOTP is not configured. */
    public Mono<Void> validate(User user, String totpCode) {
        var totpEncryptedSecret = user.getSpec().getTotpEncryptedSecret();
        if (StringUtils.isBlank(totpEncryptedSecret)) {
            // TOTP is not configured, no need to validate
            return Mono.empty();
        }
        return validate(totpEncryptedSecret, totpCode);
    }

    /** Validate the TOTP code against the given encrypted secret. */
    public Mono<Void> validate(String totpEncryptedSecret, String totpCode) {
        if (StringUtils.isBlank(totpCode)) {
            return Mono.error(new ServerWebInputException("TOTP code is required"));
        }
        int code;
        try {
            code = Integer.parseInt(totpCode);
        } catch (NumberFormatException e) {
            return Mono.error(new ServerWebInputException("Invalid TOTP code"));
        }
        var rawSecret = totpAuthService.decryptSecret(totpEncryptedSecret);
        if (!totpAuthService.validateTotp(rawSecret, code)) {
            return Mono.error(new ServerWebInputException("Invalid TOTP code"));
        }
        return Mono.empty();
    }
}
