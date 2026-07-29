package run.halo.app.security.preauth;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static run.halo.app.infra.ValidationUtils.validate;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.infra.exception.RateLimitExceededException;
import run.halo.app.infra.exception.RequestBodyValidationException;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.infra.utils.IpAddressUtils;
import run.halo.app.security.authentication.emailcode.EmailCodeService;

/**
 * Pre-auth endpoint for sending email login verification codes.
 *
 * @author johnniang
 * @since 2.26.0
 */
@Component
class PreAuthEmailCodeLoginEndpoint {

    private final EmailCodeService emailCodeService;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final Validator validator;

    PreAuthEmailCodeLoginEndpoint(
            EmailCodeService emailCodeService, RateLimiterRegistry rateLimiterRegistry, Validator validator) {
        this.emailCodeService = emailCodeService;
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.validator = validator;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    RouterFunction<ServerResponse> preAuthEmailCodeLoginEndpoints() {
        return RouterFunctions.nest(
                path("/login/email-code"),
                RouterFunctions.route()
                        .POST(
                                "/send",
                                contentType(APPLICATION_JSON),
                                request -> request.bodyToMono(SendLoginCodeBody.class)
                                        .flatMap(body -> {
                                            var bindingResult = validate(body, "body", validator, request.exchange());
                                            if (bindingResult.hasErrors()) {
                                                return Mono.error(new RequestBodyValidationException(bindingResult));
                                            }
                                            var email = body.getEmail();
                                            return emailCodeService
                                                    .sendLoginCode(email)
                                                    .transformDeferred(rateLimiterForSendingCode(
                                                            IpAddressUtils.getClientIp(request.exchange()
                                                                    .getRequest())))
                                                    .onErrorMap(
                                                            RequestNotPermitted.class, RateLimitExceededException::new);
                                        })
                                        .then(ServerResponse.accepted().build()))
                        .before(HaloUtils.noCache())
                        .build());
    }

    private <T> RateLimiterOperator<T> rateLimiterForSendingCode(String clientIp) {
        var rateLimiterKey = "send-login-email-code-from-" + clientIp;
        var rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterKey, "send-login-email-code");
        return RateLimiterOperator.of(rateLimiter);
    }

    @Data
    public static class SendLoginCodeBody {

        @Email
        @NotBlank
        String email;
    }
}
