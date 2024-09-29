package run.halo.app.security.authentication.twofactor.totp;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.security.authentication.exception.TwoFactorAuthException;
import run.halo.app.security.authentication.twofactor.TwoFactorAuthentication;

/**
 * TOTP code authentication converter.
 *
 * @author johnniang
 */
public class TotpCodeAuthenticationConverter implements ServerAuthenticationConverter {

    private final String codeParameter = "code";

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        // Check the request is authenticated before.
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(TwoFactorAuthentication.class::isInstance)
            .switchIfEmpty(Mono.error(
                () -> new TwoFactorAuthException(
                    "MFA Authentication required."
                ))
            )
            .flatMap(authentication -> exchange.getFormData())
            .handle((formData, sink) -> {
                var codeStr = formData.getFirst(codeParameter);
                if (StringUtils.isBlank(codeStr)) {
                    sink.error(new TwoFactorAuthException(
                        "Empty code parameter."
                    ));
                    return;
                }
                try {
                    var code = Integer.parseInt(codeStr);
                    sink.next(new TotpAuthenticationToken(code));
                } catch (NumberFormatException e) {
                    sink.error(new TwoFactorAuthException(
                        "Invalid code parameter " + codeStr + '.')
                    );
                }
            });
    }
}
