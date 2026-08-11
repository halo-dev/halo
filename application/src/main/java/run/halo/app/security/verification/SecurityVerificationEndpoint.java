package run.halo.app.security.verification;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import java.net.URI;
import java.util.HashMap;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.EmailVerificationService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.infra.exception.AccessDeniedException;
import run.halo.app.infra.exception.RateLimitExceededException;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.security.authentication.twofactor.TotpVerificationService;
import run.halo.app.security.authentication.twofactor.TwoFactorUtils;

/**
 * Post-auth endpoint for the security verification page (sudo mode).
 *
 * @author JohnNiang
 * @since 2.26.0
 */
@Component
@RequiredArgsConstructor
class SecurityVerificationEndpoint {

    private static final String DEFAULT_REDIRECT = "/uc/profile";

    private final UserService userService;
    private final GlobalInfoService globalInfoService;
    private final EmailVerificationService emailVerificationService;
    private final TotpVerificationService totpVerificationService;
    private final SecurityVerificationService securityVerificationService;
    private final RateLimiterRegistry rateLimiterRegistry;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    RouterFunction<ServerResponse> securityVerificationEndpoints() {
        return RouterFunctions.route()
                .GET("/security-verification", this::renderVerificationPage)
                .POST("/security-verification", this::verifySecurityVerification)
                .POST("/security-verification/email-code", this::sendEmailCode)
                .before(HaloUtils.noCache())
                .build();
    }

    private Mono<ServerResponse> renderVerificationPage(ServerRequest request) {
        var redirect = safeRedirect(request.queryParam("redirect").orElse(DEFAULT_REDIRECT));
        return currentUser(request).flatMap(user -> {
            if (!securityVerificationService.isAvailable(user)) {
                return redirectTo(redirect);
            }
            var settings = TwoFactorUtils.getTwoFactorAuthSettings(user);
            var model = new HashMap<String, Object>();
            model.put("globalInfo", globalInfoService.getGlobalInfo());
            model.put("emailVerified", settings.isEmailVerified());
            model.put("totpConfigured", settings.isTotpConfigured());
            model.put("redirect", redirect);
            return ServerResponse.ok().render("security-verification", model);
        });
    }

    private Mono<ServerResponse> verifySecurityVerification(ServerRequest request) {
        return request.formData().flatMap(formData -> {
            var redirect = safeRedirect(formData.getFirst("redirect"));
            return currentUser(request).flatMap(user -> {
                if (!securityVerificationService.isAvailable(user)) {
                    return redirectTo(redirect);
                }
                var username = user.getMetadata().getName();
                var emailCode = formData.getFirst("emailCode");
                var totpCode = formData.getFirst("totpCode");
                var verifyMono = verifyMono(user, emailCode, totpCode);
                return verifyMono
                        .transformDeferred(rateLimiterForVerification(request, username))
                        .then(request.exchange().getSession())
                        .doOnNext(securityVerificationService::markVerified)
                        .then(redirectTo(redirect))
                        .onErrorResume(
                                ServerWebInputException.class,
                                e -> redirectTo(redirectWithError("invalid-code", redirect)))
                        .onErrorResume(
                                RequestNotPermitted.class,
                                e -> redirectTo(redirectWithError("rate-limit-exceeded", redirect)));
            });
        });
    }

    private Mono<Void> verifyMono(User user, String emailCode, String totpCode) {
        if (StringUtils.isNotBlank(emailCode)) {
            return emailVerificationService.verifySecurityVerificationCode(
                    user.getMetadata().getName(), emailCode);
        }
        if (StringUtils.isNotBlank(totpCode)) {
            return totpVerificationService.validate(user, totpCode);
        }
        return Mono.error(new ServerWebInputException("Verification code is required"));
    }

    private Mono<ServerResponse> sendEmailCode(ServerRequest request) {
        return currentUser(request)
                .flatMap(user -> emailVerificationService
                        .sendSecurityVerificationCode(user.getMetadata().getName())
                        .transformDeferred(
                                rateLimiterForSendingCode(user.getMetadata().getName()))
                        .then(ServerResponse.accepted().build()))
                .onErrorMap(RequestNotPermitted.class, RateLimitExceededException::new);
    }

    private RateLimiterOperator<Void> rateLimiterForSendingCode(String username) {
        var rateLimiterKey = "send-security-verification-code-" + username;
        var rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterKey, "send-login-email-code");
        return RateLimiterOperator.of(rateLimiter);
    }

    private Function<Mono<Void>, Mono<Void>> rateLimiterForVerification(ServerRequest request, String username) {
        return mono -> request.exchange()
                .getSession()
                .map(WebSession::getId)
                .switchIfEmpty(Mono.just(username))
                // Fall back to a per-user key when no session can be derived
                // (e.g. mock test environment), so the rate limit still applies.
                .onErrorResume(throwable -> Mono.just(username))
                .flatMap(key -> {
                    var rateLimiter = rateLimiterRegistry.rateLimiter("totp-validation-" + key, "totp-validation");
                    return mono.transformDeferred(RateLimiterOperator.of(rateLimiter));
                });
    }

    private Mono<User> currentUser(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(authentication ->
                        authentication != null && !(authentication instanceof AnonymousAuthenticationToken))
                .switchIfEmpty(Mono.error(AccessDeniedException::new))
                .map(Authentication::getName)
                .flatMap(userService::getUser);
    }

    private static String safeRedirect(String redirect) {
        if (StringUtils.isNotBlank(redirect)
                && redirect.startsWith("/")
                && !redirect.startsWith("//")
                && !redirect.contains("\\")) {
            return redirect;
        }
        return DEFAULT_REDIRECT;
    }

    private static String redirectWithError(String error, String redirect) {
        return "/security-verification?error=" + error + "&redirect=" + redirect;
    }

    private static Mono<ServerResponse> redirectTo(String location) {
        return ServerResponse.status(HttpStatus.FOUND)
                .location(URI.create(location))
                .build();
    }
}
