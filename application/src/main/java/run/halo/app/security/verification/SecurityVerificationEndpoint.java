package run.halo.app.security.verification;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.EmailVerificationService;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.infra.exception.RateLimitExceededException;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.security.authentication.twofactor.TotpVerificationService;
import run.halo.app.security.authentication.twofactor.TwoFactorUtils;
import run.halo.app.security.verification.SecurityVerificationService.SecurityVerificationMethod;

/**
 * Post-auth endpoint for the security verification page (sudo mode).
 *
 * @author JohnNiang
 * @since 2.26.0
 */
@Component
@RequiredArgsConstructor
class SecurityVerificationEndpoint {

    private final GlobalInfoService globalInfoService;
    private final EmailVerificationService emailVerificationService;
    private final TotpVerificationService totpVerificationService;
    private final SecurityVerificationService securityVerificationService;
    private final SecurityVerificationFlowService flowService;
    private final RateLimiterRegistry rateLimiterRegistry;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    RouterFunction<ServerResponse> securityVerificationEndpoints() {
        return RouterFunctions.route()
                .GET("/security-verification", this::renderVerificationPage)
                .POST("/security-verification/email", this::verifyByEmail)
                .POST("/security-verification/totp", this::verifyByTotp)
                .POST("/security-verification/email-code", this::sendEmailCode)
                .before(HaloUtils.noCache())
                .build();
    }

    private Mono<ServerResponse> renderVerificationPage(ServerRequest request) {
        var redirect = flowService.safeRedirect(
                request.queryParam("redirect").orElse(SecurityVerificationFlowService.DEFAULT_REDIRECT));
        return flowService.currentUser(request).flatMap(user -> {
            var availableMethods = securityVerificationService.availableMethods(user);
            if (availableMethods.isEmpty()) {
                return flowService.redirectTo(redirect);
            }
            var model = new HashMap<String, Object>();
            model.put("globalInfo", globalInfoService.getGlobalInfo());
            model.put("redirect", redirect);
            model.put("availableMethods", availableMethods);
            model.put("fragmentTemplateName", resolveFragmentTemplateName(request, availableMethods));
            return ServerResponse.ok().render("security-verification", model);
        });
    }

    private static String resolveFragmentTemplateName(
            ServerRequest request, List<SecurityVerificationMethod> availableMethods) {
        var requestedMethod = request.queryParam("method").orElse(null);
        return availableMethods.stream()
                .filter(method -> Objects.equals(requestedMethod, method.name()))
                .map(SecurityVerificationMethod::fragmentTemplateName)
                .findFirst()
                .orElseGet(() -> availableMethods.get(0).fragmentTemplateName());
    }

    private Mono<ServerResponse> verifyByEmail(ServerRequest request) {
        return request.formData().flatMap(formData -> {
            var redirect = flowService.safeRedirect(formData.getFirst("redirect"));
            var emailCode = formData.getFirst("emailCode");
            return flowService.currentUser(request).flatMap(user -> {
                if (securityVerificationService.availableMethods(user).isEmpty()) {
                    return flowService.redirectTo(redirect);
                }
                if (!TwoFactorUtils.getTwoFactorAuthSettings(user).isEmailVerified()) {
                    return flowService.redirectTo(flowService.redirectWithError("invalid-code", redirect, "email"));
                }
                if (StringUtils.isBlank(emailCode)) {
                    return flowService.redirectTo(flowService.redirectWithError("invalid-code", redirect, "email"));
                }
                return flowService.verifyAndRedirect(
                        request,
                        redirect,
                        "email",
                        emailVerificationService.verifySecurityVerificationCode(
                                user.getMetadata().getName(), emailCode));
            });
        });
    }

    private Mono<ServerResponse> verifyByTotp(ServerRequest request) {
        return request.formData().flatMap(formData -> {
            var redirect = flowService.safeRedirect(formData.getFirst("redirect"));
            var totpCode = formData.getFirst("totpCode");
            return flowService.currentUser(request).flatMap(user -> {
                if (securityVerificationService.availableMethods(user).isEmpty()) {
                    return flowService.redirectTo(redirect);
                }
                if (!TwoFactorUtils.getTwoFactorAuthSettings(user).isTotpConfigured()) {
                    return flowService.redirectTo(flowService.redirectWithError("invalid-code", redirect, "totp"));
                }
                return flowService.verifyAndRedirect(
                        request, redirect, "totp", totpVerificationService.validate(user, totpCode));
            });
        });
    }

    private Mono<ServerResponse> sendEmailCode(ServerRequest request) {
        return flowService
                .currentUser(request)
                .flatMap(user -> emailVerificationService
                        .sendSecurityVerificationCode(user.getMetadata().getName())
                        .transformDeferred(
                                rateLimiterForSendingCode(user.getMetadata().getName()))
                        .then(ServerResponse.accepted().build()))
                .onErrorMap(RequestNotPermitted.class, RateLimitExceededException::new);
    }

    private Function<Mono<Void>, Mono<Void>> rateLimiterForSendingCode(String username) {
        var rateLimiter =
                rateLimiterRegistry.rateLimiter("send-security-verification-code-" + username, "send-login-email-code");
        var operator = RateLimiterOperator.<Void>of(rateLimiter);
        return mono -> mono.transformDeferred(operator);
    }
}
