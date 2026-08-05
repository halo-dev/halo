package run.halo.app.security.completion;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static run.halo.app.infra.ValidationUtils.validate;
import static run.halo.app.security.RedirectUtils.redirectToSavedRequest;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.EmailVerificationService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.infra.exception.EmailVerificationFailed;
import run.halo.app.infra.exception.RateLimitExceededException;
import run.halo.app.infra.exception.RequestBodyValidationException;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.infra.utils.IpAddressUtils;

/** Gateway endpoints for completing the authenticated user's email. */
@Component
@RequiredArgsConstructor
class EmailCompletionEndpoint {

    private final UserService userService;
    private final SystemConfigFetcher systemConfigFetcher;
    private final EmailVerificationService emailVerificationService;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final ReactiveExtensionClient client;
    private final ServerRequestCache requestCache;
    private final GlobalInfoService globalInfoService;
    private final Validator validator;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    RouterFunction<ServerResponse> emailCompletionEndpoints() {
        return RouterFunctions.nest(
                path("/complete-profile"),
                RouterFunctions.route()
                        .GET("", this::page)
                        .POST("", this::submit)
                        .POST("/send-email-code", this::sendEmailCode)
                        .before(HaloUtils.noCache())
                        .build());
    }

    private Mono<ServerResponse> page(ServerRequest request) {
        return currentUsername(request.exchange())
                .flatMap(username -> userService.getUser(username))
                .flatMap(user -> systemConfigFetcher
                        .fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
                        .flatMap(setting -> {
                            var form = new CompleteProfileForm(user.getSpec().getEmail(), null);
                            var bindingResult = new BeanPropertyBindingResult(form, "form");
                            return renderPage(request.exchange(), setting, form, bindingResult);
                        }));
    }

    private Mono<ServerResponse> submit(ServerRequest request) {
        var exchange = request.exchange();
        return request.formData().flatMap(form -> {
            var completeForm = new CompleteProfileForm(form.getFirst("email"), form.getFirst("code"));
            var bindingResult = validate(completeForm, "form", validator, exchange);
            return currentUsername(exchange)
                    .flatMap(username -> handleSubmit(exchange, username, completeForm, bindingResult));
        });
    }

    private Mono<ServerResponse> handleSubmit(
            ServerWebExchange exchange, String username, CompleteProfileForm form, BindingResult bindingResult) {
        return Mono.zip(
                        userService.getUser(username),
                        systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .flatMap(tuple -> {
                    var user = tuple.getT1();
                    var setting = tuple.getT2();
                    if (bindingResult.hasErrors()) {
                        return renderPage(exchange, setting, form, bindingResult);
                    }
                    var email = form.email().toLowerCase(Locale.ROOT);
                    return userService.checkVerifiedEmailInUse(username, email).flatMap(inUse -> {
                        if (inUse) {
                            bindingResult.rejectValue(
                                    "email", "complete-profile.error.email-in-use", "Email is already in use");
                            return renderPage(exchange, setting, form, bindingResult);
                        }
                        if (setting.isMustVerifyEmailOnRegistration() && StringUtils.isBlank(form.code())) {
                            bindingResult.rejectValue(
                                    "code", "complete-profile.error.code-required", "Email code is required");
                            return renderPage(exchange, setting, form, bindingResult);
                        }
                        return verifyOrSave(exchange, user, setting, form, bindingResult);
                    });
                });
    }

    private Mono<ServerResponse> verifyOrSave(
            ServerWebExchange exchange,
            User user,
            SystemSetting.User setting,
            CompleteProfileForm form,
            BindingResult bindingResult) {
        var username = user.getMetadata().getName();
        var email = form.email().toLowerCase(Locale.ROOT);
        if (StringUtils.isNotBlank(form.code())) {
            var emailToVerify = MetadataUtil.nullSafeAnnotations(user).get(User.EMAIL_TO_VERIFY);
            if (!StringUtils.equalsIgnoreCase(emailToVerify, email)) {
                bindingResult.rejectValue(
                        "email",
                        "complete-profile.error.email-mismatch",
                        "The email does not match the address the code was sent to");
                return renderPage(exchange, setting, form, bindingResult);
            }
            return emailVerificationService
                    .verify(username, form.code())
                    .then(redirectToTarget(exchange))
                    .onErrorResume(EmailVerificationFailed.class, e -> {
                        bindingResult.rejectValue("code", "complete-profile.error.invalid-code", "Invalid email code");
                        return renderPage(exchange, setting, form, bindingResult);
                    });
        }
        user.getSpec().setEmail(email);
        // The new email has not been verified; reset the flag so the user must verify it
        // before it can be used for password reset.
        user.getSpec().setEmailVerified(false);
        return client.update(user).then(redirectToTarget(exchange));
    }

    private Mono<ServerResponse> sendEmailCode(ServerRequest request) {
        var exchange = request.exchange();
        return request.bodyToMono(SendEmailCodeBody.class)
                .flatMap(body -> {
                    var bindingResult = validate(body, "body", validator, exchange);
                    if (bindingResult.hasErrors()) {
                        return Mono.error(new RequestBodyValidationException(bindingResult));
                    }
                    var email = body.email().toLowerCase(Locale.ROOT);
                    return currentUsername(exchange)
                            .flatMap(username -> userService
                                    .checkVerifiedEmailInUse(username, email)
                                    .flatMap(inUse -> {
                                        if (inUse) {
                                            return Mono.error(new EmailVerificationFailed(
                                                    "Email already in use.",
                                                    null,
                                                    "problemDetail.user.email.verify.emailInUse",
                                                    null));
                                        }
                                        return emailVerificationService
                                                .sendVerificationCode(username, email)
                                                .transformDeferred(sendCodeRateLimiter(exchange))
                                                .onErrorMap(RequestNotPermitted.class, RateLimitExceededException::new);
                                    }));
                })
                .then(ServerResponse.accepted().build());
    }

    private Mono<ServerResponse> renderPage(
            ServerWebExchange exchange,
            SystemSetting.User setting,
            CompleteProfileForm form,
            BindingResult bindingResult) {
        var model = new HashMap<String, Object>();
        model.put("globalInfo", globalInfoService.getGlobalInfo().cache());
        model.put("mustVerifyEmailOnRegistration", setting.isMustVerifyEmailOnRegistration());
        model.putAll(bindingResult.getModel());
        return ServerResponse.ok().render("complete_profile", model);
    }

    private Mono<ServerResponse> redirectToTarget(ServerWebExchange exchange) {
        return redirectToSavedRequest(requestCache, exchange, URI.create("/uc"));
    }

    private Mono<String> currentUsername(ServerWebExchange exchange) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(java.security.Principal::getName);
    }

    private <T> RateLimiterOperator<T> sendCodeRateLimiter(ServerWebExchange exchange) {
        var clientIp = IpAddressUtils.getClientIp(exchange.getRequest());
        var rateLimiterKey = "send-email-verification-code-from-" + clientIp;
        var rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterKey, "send-email-verification-code");
        return RateLimiterOperator.of(rateLimiter);
    }

    public record CompleteProfileForm(@NotBlank @Email String email, String code) {}

    public record SendEmailCodeBody(@NotBlank @Email String email) {}
}
