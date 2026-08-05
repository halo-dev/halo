package run.halo.app.security.preauth;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static run.halo.app.infra.ValidationUtils.validate;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.EmailVerificationService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.EmailAlreadyTakenException;
import run.halo.app.infra.exception.EmailVerificationFailed;
import run.halo.app.infra.exception.RateLimitExceededException;
import run.halo.app.infra.exception.RequestBodyValidationException;
import run.halo.app.infra.utils.HaloUtils;

@Component
@RequiredArgsConstructor
class CompleteProfileEndpoint {

    private final UserService userService;

    private final SystemConfigFetcher systemConfigFetcher;

    private final EmailVerificationService emailVerificationService;

    private final RateLimiterRegistry rateLimiterRegistry;

    private final ReactiveExtensionClient client;

    private final ServerRequestCache requestCache;

    private final Validator validator;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    RouterFunction<ServerResponse> completeProfileEndpoints() {
        return RouterFunctions.nest(
                path("/complete-profile"),
                RouterFunctions.route()
                        .GET("", this::renderPage)
                        .POST("/send-email-code", contentType(APPLICATION_JSON), this::sendEmailCode)
                        .POST("", contentType(APPLICATION_FORM_URLENCODED), this::completeProfile)
                        .before(HaloUtils.noCache())
                        .build());
    }

    private Mono<ServerResponse> renderPage(ServerRequest request) {
        return currentProfile(request).flatMap(profile -> {
            if (profile.user().getSpec().isEmailVerified()) {
                return redirectToSavedRequest(request);
            }
            var form = new CompleteProfileForm(profile.user().getSpec().getEmail(), null);
            return renderForm(profile.setting(), form);
        });
    }

    private Mono<ServerResponse> sendEmailCode(ServerRequest request) {
        return currentProfile(request).flatMap(profile -> {
            if (profile.user().getSpec().isEmailVerified()) {
                return redirectToSavedRequest(request);
            }
            return request.bodyToMono(SendEmailCodeBody.class)
                    .switchIfEmpty(Mono.error(() -> new ServerWebInputException("Request body is required.")))
                    .flatMap(body -> {
                        var bindingResult = validate(body, "body", validator, request.exchange());
                        if (bindingResult.hasErrors()) {
                            return Mono.error(new RequestBodyValidationException(bindingResult));
                        }
                        var email = normalizeEmail(body.email());
                        return userService.checkEmailAlreadyVerified(email).flatMap(emailTaken -> {
                            if (emailTaken) {
                                return Mono.error(new EmailAlreadyTakenException("Email already taken."));
                            }
                            return Mono.just(profile.username())
                                    .transformDeferred(sendEmailVerificationCodeRateLimiter(profile.username()))
                                    .flatMap(ignored ->
                                            emailVerificationService.sendVerificationCode(profile.username(), email))
                                    .onErrorMap(RequestNotPermitted.class, RateLimitExceededException::new);
                        });
                    })
                    .then(ServerResponse.accepted().build());
        });
    }

    private Mono<ServerResponse> completeProfile(ServerRequest request) {
        return currentProfile(request).flatMap(profile -> {
            if (profile.user().getSpec().isEmailVerified()) {
                return redirectToSavedRequest(request);
            }
            return request.formData().flatMap(formData -> {
                var form = new CompleteProfileForm(formData.getFirst("email"), formData.getFirst("emailCode"));
                var bindingResult = validate(form, "form", validator, request.exchange());
                if (bindingResult.hasFieldErrors("email")) {
                    return renderFormError(
                            profile.setting(),
                            form,
                            "email",
                            "form.error.emailInvalid",
                            "Enter a valid email address.");
                }
                var email = normalizeEmail(form.email());
                return userService.checkEmailAlreadyVerified(email).flatMap(emailTaken -> {
                    if (emailTaken) {
                        return renderFormError(
                                profile.setting(),
                                form,
                                "email",
                                "form.error.emailTaken",
                                "This email address is already used by another user.");
                    }
                    return verifyOrSave(request, profile, form, email);
                });
            });
        });
    }

    private Mono<ServerResponse> verifyOrSave(
            ServerRequest request, CurrentProfile profile, CompleteProfileForm form, String email) {
        var code = form.emailCode();
        if (profile.setting().isMustVerifyEmailOnRegistration() && StringUtils.isBlank(code)) {
            return renderFormError(
                    profile.setting(),
                    form,
                    "emailCode",
                    "form.error.codeRequired",
                    "Enter the email verification code.");
        }
        if (StringUtils.isNotBlank(code)) {
            return verifyEmail(request, profile, form, email, code);
        }
        profile.user().getSpec().setEmail(email);
        profile.user().getSpec().setEmailVerified(false);
        return client.update(profile.user()).then(redirectToSavedRequest(request));
    }

    private Mono<ServerResponse> verifyEmail(
            ServerRequest request, CurrentProfile profile, CompleteProfileForm form, String email, String code) {
        var emailToVerify = MetadataUtil.nullSafeAnnotations(profile.user()).get(User.EMAIL_TO_VERIFY);
        if (!StringUtils.equalsIgnoreCase(emailToVerify, email)) {
            return renderFormError(
                    profile.setting(),
                    form,
                    "email",
                    "form.error.emailChanged",
                    "The email address changed. Send a new verification code.");
        }
        return Mono.just(profile.username())
                .transformDeferred(verificationEmailRateLimiter(profile.username()))
                .flatMap(ignored -> emailVerificationService.verify(profile.username(), code))
                .onErrorMap(RequestNotPermitted.class, RateLimitExceededException::new)
                .then(redirectToSavedRequest(request))
                .onErrorResume(
                        EmailVerificationFailed.class,
                        error -> renderFormError(
                                profile.setting(),
                                form,
                                "emailCode",
                                "form.error.codeInvalid",
                                "The verification code is invalid or expired."))
                .onErrorResume(
                        RateLimitExceededException.class,
                        error -> renderFormError(
                                profile.setting(),
                                form,
                                "emailCode",
                                "form.error.rateLimitExceeded",
                                "Too many attempts. Try again later."));
    }

    private Mono<CurrentProfile> currentProfile(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(authentication -> authentication.getName())
                .flatMap(username -> Mono.zip(
                                userService.getUser(username),
                                systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                        .map(tuple -> new CurrentProfile(username, tuple.getT1(), tuple.getT2())));
    }

    private Mono<ServerResponse> renderForm(SystemSetting.User setting, CompleteProfileForm form) {
        var bindingResult = new BeanPropertyBindingResult(form, "form");
        var model = bindingResult.getModel();
        model.put("mustVerifyEmailOnRegistration", setting.isMustVerifyEmailOnRegistration());
        return ServerResponse.ok().render("complete_profile", model);
    }

    private Mono<ServerResponse> renderFormError(
            SystemSetting.User setting, CompleteProfileForm form, String field, String code, String defaultMessage) {
        var bindingResult = new BeanPropertyBindingResult(form, "form");
        bindingResult.rejectValue(field, code, defaultMessage);
        var model = bindingResult.getModel();
        model.put("mustVerifyEmailOnRegistration", setting.isMustVerifyEmailOnRegistration());
        return ServerResponse.ok().render("complete_profile", model);
    }

    private Mono<ServerResponse> redirectToSavedRequest(ServerRequest request) {
        return requestCache
                .getRedirectUri(request.exchange())
                .defaultIfEmpty(URI.create("/uc"))
                .flatMap(uri ->
                        ServerResponse.status(HttpStatus.FOUND).location(uri).build());
    }

    private <T> RateLimiterOperator<T> sendEmailVerificationCodeRateLimiter(String username) {
        var rateLimiter = rateLimiterRegistry.rateLimiter(
                "send-email-verification-code-" + username, "send-email-verification-code");
        return RateLimiterOperator.of(rateLimiter);
    }

    private <T> RateLimiterOperator<T> verificationEmailRateLimiter(String username) {
        var rateLimiter = rateLimiterRegistry.rateLimiter("verify-email-" + username, "verify-email");
        return RateLimiterOperator.of(rateLimiter);
    }

    private static String normalizeEmail(String email) {
        return email.toLowerCase(Locale.ROOT);
    }

    record SendEmailCodeBody(@Email @NotBlank String email) {}

    record CompleteProfileForm(@Email @NotBlank String email, String emailCode) {}

    private record CurrentProfile(String username, User user, SystemSetting.User setting) {}
}
