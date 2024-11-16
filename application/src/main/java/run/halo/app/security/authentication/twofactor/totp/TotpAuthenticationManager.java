package run.halo.app.security.authentication.twofactor.totp;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;
import run.halo.app.security.HaloUserDetails;
import run.halo.app.security.authentication.exception.TwoFactorAuthException;
import run.halo.app.security.authentication.twofactor.TwoFactorAuthentication;

/**
 * TOTP authentication manager.
 *
 * @author johnniang
 */
@Slf4j
public class TotpAuthenticationManager implements ReactiveAuthenticationManager {

    private final TotpAuthService totpAuthService;

    public TotpAuthenticationManager(TotpAuthService totpAuthService) {
        this.totpAuthService = totpAuthService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        // it should be TotpAuthenticationToken
        var code = (Integer) authentication.getCredentials();
        log.debug("Got TOTP code {}", code);

        // get user details
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .cast(TwoFactorAuthentication.class)
            .map(TwoFactorAuthentication::getPrevious)
            .flatMap(previousAuth -> {
                var principal = previousAuth.getPrincipal();
                if (!(principal instanceof HaloUserDetails user)) {
                    return Mono.error(
                        new TwoFactorAuthException("Invalid authentication principal.")
                    );
                }
                var totpEncryptedSecret = user.getTotpEncryptedSecret();
                if (StringUtils.isBlank(totpEncryptedSecret)) {
                    return Mono.error(
                        new TwoFactorAuthException("TOTP secret not configured.")
                    );
                }
                var rawSecret = totpAuthService.decryptSecret(totpEncryptedSecret);
                var validated = totpAuthService.validateTotp(rawSecret, code);
                if (!validated) {
                    return Mono.error(new TwoFactorAuthException("Invalid TOTP code " + code));
                }
                if (log.isDebugEnabled()) {
                    log.debug(
                        "TOTP authentication for {} with code {} successfully.",
                        previousAuth.getName(), code);
                }
                if (previousAuth instanceof CredentialsContainer container) {
                    container.eraseCredentials();
                }
                return Mono.just(previousAuth);
            });
    }
}
