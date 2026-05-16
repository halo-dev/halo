package run.halo.app.security.authentication.twofactor.totp;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.infra.utils.IpAddressUtils;
import run.halo.app.security.authentication.exception.TooManyRequestsException;
import run.halo.app.security.authentication.exception.TwoFactorAuthException;
import run.halo.app.security.authentication.twofactor.TwoFactorAuthentication;

/**
 * TOTP code authentication converter.
 *
 * @author johnniang
 */
public class TotpCodeAuthenticationConverter implements ServerAuthenticationConverter {

    private final String codeParameter = "code";

    private final RateLimiterRegistry rateLimiterRegistry;

    public TotpCodeAuthenticationConverter(RateLimiterRegistry rateLimiterRegistry) {
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        // Check the request is authenticated before.
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(TwoFactorAuthentication.class::isInstance)
                .switchIfEmpty(Mono.error(() -> new TwoFactorAuthException("MFA Authentication required.")))
                .flatMap(authentication -> exchange.getFormData())
                .handle((formData, sink) -> {
                    var codeStr = formData.getFirst(codeParameter);
                    if (StringUtils.isBlank(codeStr)) {
                        sink.error(new TwoFactorAuthException("Empty code parameter."));
                        return;
                    }
                    try {
                        var code = Integer.parseInt(codeStr);
                        sink.next(new TotpAuthenticationToken(code));
                    } catch (NumberFormatException e) {
                        sink.error(new TwoFactorAuthException("Invalid code parameter " + codeStr + '.'));
                    }
                })
                .transformDeferred(createRateLimiter(exchange))
                .onErrorMap(RequestNotPermitted.class, TooManyRequestsException::new);
    }

    private <T> RateLimiterOperator<T> createRateLimiter(ServerWebExchange exchange) {
        var sessionCookie = exchange.getRequest().getCookies().getFirst("SESSION");
        var key = "totp-validation-"
                + (sessionCookie != null
                        ? sessionCookie.getValue()
                        : IpAddressUtils.getClientIp(exchange.getRequest()));
        return RateLimiterOperator.of(rateLimiterRegistry.rateLimiter(key, "totp-validation"));
    }
}
