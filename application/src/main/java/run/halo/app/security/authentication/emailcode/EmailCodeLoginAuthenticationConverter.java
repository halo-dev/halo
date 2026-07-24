package run.halo.app.security.authentication.emailcode;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.infra.utils.IpAddressUtils;
import run.halo.app.security.authentication.exception.TooManyRequestsException;

/**
 * Converts a form POST with {@code email} and {@code code} fields into an {@link EmailCodeAuthenticationToken}.
 *
 * @author johnniang
 * @since 2.26.0
 */
public class EmailCodeLoginAuthenticationConverter implements ServerAuthenticationConverter {

    static final String EMAIL_PARAMETER_NAME = "email";
    static final String CODE_PARAMETER_NAME = "code";

    private final RateLimiterRegistry rateLimiterRegistry;

    public EmailCodeLoginAuthenticationConverter(RateLimiterRegistry rateLimiterRegistry) {
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return exchange.getFormData()
                .flatMap(formData -> {
                    var email = formData.getFirst(EMAIL_PARAMETER_NAME);
                    var code = formData.getFirst(CODE_PARAMETER_NAME);
                    if (email == null || code == null) {
                        return Mono.error(() -> new IllegalArgumentException("Email and code must not be null"));
                    }
                    return Mono.just((Authentication) new EmailCodeAuthenticationToken(email, code));
                })
                .transformDeferred(rateLimiter(exchange))
                .onErrorMap(RequestNotPermitted.class, TooManyRequestsException::new);
    }

    private <T> RateLimiterOperator<T> rateLimiter(ServerWebExchange exchange) {
        var clientIp = IpAddressUtils.getClientIp(exchange.getRequest());
        var rateLimiterKey = "verify-login-email-code-from-" + clientIp;
        var rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterKey, "verify-login-email-code");
        return RateLimiterOperator.of(rateLimiter);
    }
}
