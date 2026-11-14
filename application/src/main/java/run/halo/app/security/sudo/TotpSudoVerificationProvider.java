package run.halo.app.security.sudo;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.security.authentication.twofactor.totp.TotpAuthService;

/**
 * TOTP-based sudo confirmation.
 *
 * @author johnniang
 * @since 2.27.0
 */
@Component
@Order(0)
class TotpSudoVerificationProvider implements SudoVerificationProvider {

    static final String METHOD = "totp";

    private final TotpAuthService totpAuthService;

    TotpSudoVerificationProvider(TotpAuthService totpAuthService) {
        this.totpAuthService = totpAuthService;
    }

    @Override
    public String method() {
        return METHOD;
    }

    @Override
    public boolean canSendCode() {
        return false;
    }

    @Override
    public Mono<Boolean> supports(User user) {
        return Mono.just(StringUtils.isNotBlank(user.getSpec().getTotpEncryptedSecret()));
    }

    @Override
    public Mono<Void> sendCode(User user) {
        return Mono.error(new ServerWebInputException("TOTP does not send a verification code"));
    }

    @Override
    public Mono<Void> verify(User user, String code) {
        var encryptedSecret = user.getSpec().getTotpEncryptedSecret();
        if (StringUtils.isBlank(encryptedSecret) || StringUtils.isBlank(code)) {
            return Mono.error(SudoVerificationFailedException::new);
        }
        int totpCode;
        try {
            totpCode = Integer.parseInt(code);
        } catch (NumberFormatException e) {
            return Mono.error(SudoVerificationFailedException::new);
        }
        var rawSecret = totpAuthService.decryptSecret(encryptedSecret);
        if (!totpAuthService.validateTotp(rawSecret, totpCode)) {
            return Mono.error(SudoVerificationFailedException::new);
        }
        return Mono.empty();
    }
}
