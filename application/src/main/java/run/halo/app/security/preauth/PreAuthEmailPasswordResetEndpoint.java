package run.halo.app.security.preauth;

import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static run.halo.app.infra.ValidationUtils.validate;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Data;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.EmailPasswordRecoveryService;
import run.halo.app.core.user.service.InvalidResetTokenException;
import run.halo.app.infra.ValidationUtils;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.infra.utils.IpAddressUtils;

/**
 * Pre-auth password reset endpoint.
 *
 * @author johnniang
 * @since 2.20.0
 */
@Component
class PreAuthEmailPasswordResetEndpoint {

    private static final String SEND_TEMPLATE = "password-reset/email/send";
    private static final String RESET_TEMPLATE = "password-reset/email/reset";

    private final RateLimiterRegistry rateLimiterRegistry;

    public PreAuthEmailPasswordResetEndpoint(
        RateLimiterRegistry rateLimiterRegistry
    ) {
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    RouterFunction<ServerResponse> preAuthPasswordResetEndpoints(
        GlobalInfoService globalInfoService,
        PasswordResetAvailabilityProviders availabilityProviders,
        MessageSource messageSource,
        EmailPasswordRecoveryService emailService,
        Validator validator
    ) {
        return RouterFunctions.nest(path("/password-reset/email"), RouterFunctions.route()
            .GET("", request -> request.bind(SendForm.class)
                .flatMap(sendForm -> ServerResponse.ok().render(SEND_TEMPLATE, Map.of(
                    "otherMethods", availabilityProviders.getOtherAvailableMethods("email"),
                    "globalInfo", globalInfoService.getGlobalInfo(),
                    "form", sendForm
                )))
            )
            .GET("/{resetToken}",
                request -> {
                    var token = request.pathVariable("resetToken");
                    return request.bind(ResetForm.class)
                        .flatMap(resetForm -> {
                            var model = new HashMap<String, Object>();
                            model.put("form", resetForm);
                            model.put("globalInfo", globalInfoService.getGlobalInfo());
                            return emailService.getValidResetToken(token)
                                .flatMap(resetToken -> {
                                    // TODO Check the 2FA of the user
                                    model.put("username", resetToken.username());
                                    return ServerResponse.ok().render(RESET_TEMPLATE, model);
                                })
                                .transformDeferred(rateLimiterForPasswordResetVerification(
                                    request.exchange().getRequest()
                                ))
                                .onErrorResume(InvalidResetTokenException.class, e ->
                                    ServerResponse.status(HttpStatus.FOUND)
                                        .location(URI.create(
                                            "/password-reset/email?error=invalid_reset_token")
                                        )
                                        .build()
                                )
                                .onErrorResume(RequestNotPermitted.class, e -> {
                                    model.put("error", "rate_limit_exceeded");
                                    return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                                        .render(RESET_TEMPLATE, model);
                                });
                        });
                }
            )
            .POST("/{resetToken}", request -> {
                var token = request.pathVariable("resetToken");
                return request.bind(ResetForm.class)
                    .flatMap(resetForm -> emailService.getValidResetToken(token)
                        .flatMap(resetToken -> {
                            var bindingResult = validate(resetForm, validator, request.exchange());
                            var model = bindingResult.getModel();
                            model.put("globalInfo", globalInfoService.getGlobalInfo());
                            model.put("username", resetToken.username());
                            if (!Objects.equals(
                                resetForm.getPassword(), resetForm.getConfirmPassword()
                            )) {
                                bindingResult.rejectValue(
                                    "confirmPassword",
                                    "validation.error.password.confirmPassword.mismatch",
                                    "Password and confirm password mismatch"
                                );
                            }
                            if (bindingResult.hasErrors()) {
                                return ServerResponse.badRequest().render(RESET_TEMPLATE, model);
                            }
                            return emailService.changePassword(resetForm.getPassword(), token)
                                .then(ServerResponse.status(HttpStatus.FOUND)
                                    .location(URI.create("/login?password_reset"))
                                    .build()
                                )
                                .transformDeferred(rateLimiterForPasswordResetVerification(
                                    request.exchange().getRequest()
                                ))
                                .onErrorResume(RequestNotPermitted.class, e -> {
                                    model.put("error", "rate_limit_exceeded");
                                    return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                                        .render(RESET_TEMPLATE, model);
                                });
                        })
                        .onErrorResume(InvalidResetTokenException.class,
                            e -> ServerResponse.status(HttpStatus.FOUND)
                                .location(URI.create(
                                    "/password-reset/email?error=invalid_reset_token"
                                ))
                                .build()
                        )
                    );
            })
            .POST("", contentType(MediaType.APPLICATION_FORM_URLENCODED),
                request -> request.bind(SendForm.class)
                    .flatMap(sendForm -> {
                        // validate the send form
                        var bindingResult = validate(sendForm, validator, request.exchange());
                        var model = bindingResult.getModel();
                        model.put("globalInfo", globalInfoService.getGlobalInfo());
                        if (bindingResult.hasErrors()) {
                            return ServerResponse.badRequest().render(SEND_TEMPLATE, model);
                        }
                        return emailService.sendPasswordResetEmail(sendForm.getEmail())
                            .then(Mono.defer(() -> {
                                model.put("sent", true);
                                return ServerResponse.ok().render(SEND_TEMPLATE, model);
                            }))
                            .transformDeferred(rateLimiterForSendPasswordResetEmail(
                                request.exchange().getRequest()
                            ))
                            .onErrorResume(RequestNotPermitted.class, e -> {
                                model.put("error", "rate_limit_exceeded");
                                return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                                    .render(SEND_TEMPLATE, model);
                            });
                    })
            )
            .before(HaloUtils.noCache())
            .build());
    }

    <T> RateLimiterOperator<T> rateLimiterForSendPasswordResetEmail(ServerHttpRequest request) {
        var clientIp = IpAddressUtils.getClientIp(request);
        var rateLimiterKey = "send-password-reset-email-from-" + clientIp;
        var rateLimiter =
            rateLimiterRegistry.rateLimiter(rateLimiterKey, "send-password-reset-email");
        return RateLimiterOperator.of(rateLimiter);
    }

    <T> RateLimiterOperator<T> rateLimiterForPasswordResetVerification(ServerHttpRequest request) {
        var clientIp = IpAddressUtils.getClientIp(request);
        var rateLimiterKey = "password-reset-email-verify-from-" + clientIp;
        var rateLimiter =
            rateLimiterRegistry.rateLimiter(rateLimiterKey, "password-reset-verification");
        return RateLimiterOperator.of(rateLimiter);
    }

    @Data
    static class ResetForm {

        @NotBlank
        @Pattern(
            regexp = ValidationUtils.PASSWORD_REGEX,
            message = "{validation.error.password.pattern}"
        )
        @Size(min = 5, max = 257)
        private String password;

        @NotBlank
        private String confirmPassword;

    }

    @Data
    static class SendForm {

        @NotBlank
        @Email
        private String email;

    }
}
