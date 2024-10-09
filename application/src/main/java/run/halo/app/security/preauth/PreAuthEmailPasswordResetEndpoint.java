package run.halo.app.security.preauth;

import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.core.user.service.EmailPasswordRecoveryService;
import run.halo.app.core.user.service.InvalidResetTokenException;
import run.halo.app.infra.exception.RateLimitExceededException;
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

    private final EmailPasswordRecoveryService emailPasswordRecoveryService;

    private final MessageSource messageSource;

    private final RateLimiterRegistry rateLimiterRegistry;

    private final PasswordResetAvailabilityProviders passwordResetAvailabilityProviders;

    public PreAuthEmailPasswordResetEndpoint(
        EmailPasswordRecoveryService emailPasswordRecoveryService,
        MessageSource messageSource,
        RateLimiterRegistry rateLimiterRegistry,
        PasswordResetAvailabilityProviders passwordResetAvailabilityProviders
    ) {
        this.emailPasswordRecoveryService = emailPasswordRecoveryService;
        this.messageSource = messageSource;
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.passwordResetAvailabilityProviders = passwordResetAvailabilityProviders;
    }

    @Bean
    RouterFunction<ServerResponse> preAuthPasswordResetEndpoints() {
        return RouterFunctions.nest(path("/password-reset/email"), RouterFunctions.route()
            .GET("", request -> {
                return ServerResponse.ok().render(SEND_TEMPLATE, Map.of(
                    "otherMethods",
                    passwordResetAvailabilityProviders.getOtherAvailableMethods("email")
                ));
            })
            .GET("/{resetToken}",
                request -> {
                    var token = request.pathVariable("resetToken");
                    return emailPasswordRecoveryService.getValidResetToken(token)
                        .flatMap(resetToken -> {
                            // TODO Check the 2FA of the user
                            return ServerResponse.ok().render(RESET_TEMPLATE, Map.of(
                                "username", resetToken.username()
                            ));
                        })
                        .onErrorResume(InvalidResetTokenException.class,
                            e -> ServerResponse.status(HttpStatus.FOUND)
                                .location(
                                    URI.create("/password-reset/email?error=invalid_reset_token")
                                )
                                .build()
                                .transformDeferred(rateLimiterForPasswordResetVerification(
                                    request.exchange().getRequest()
                                ))
                                .onErrorMap(
                                    RequestNotPermitted.class, RateLimitExceededException::new
                                )
                        );
                }
            )
            .POST("/{resetToken}", request -> {
                var token = request.pathVariable("resetToken");
                return request.formData()
                    .flatMap(formData -> {
                        var locale = Optional.ofNullable(
                                request.exchange().getLocaleContext().getLocale()
                            )
                            .orElseGet(Locale::getDefault);
                        var password = formData.getFirst("password");
                        var confirmPassword = formData.getFirst("confirmPassword");
                        if (StringUtils.isBlank(password)) {
                            var error = messageSource.getMessage(
                                "passwordReset.password.blank",
                                null,
                                "Password can't be blank",
                                locale
                            );
                            return ServerResponse.ok().render(RESET_TEMPLATE, Map.of(
                                "error", error
                            ));
                        }
                        if (!Objects.equals(password, confirmPassword)) {
                            var error = messageSource.getMessage(
                                "passwordReset.confirmPassword.mismatch",
                                null,
                                "Password and confirm password mismatch",
                                locale
                            );
                            return ServerResponse.ok().render(RESET_TEMPLATE, Map.of(
                                "error", error
                            ));
                        }
                        return emailPasswordRecoveryService.changePassword(password, token)
                            .then(ServerResponse.status(HttpStatus.FOUND)
                                .location(URI.create("/login?password_reset"))
                                .build()
                            )
                            .onErrorResume(InvalidResetTokenException.class, e -> {
                                var error = messageSource.getMessage(
                                    "passwordReset.resetToken.invalid",
                                    null,
                                    "Invalid reset token",
                                    locale
                                );
                                return ServerResponse.ok().render(RESET_TEMPLATE, Map.of(
                                    "error", error
                                )).transformDeferred(rateLimiterForPasswordResetVerification(
                                    request.exchange().getRequest()
                                )).onErrorMap(
                                    RequestNotPermitted.class, RateLimitExceededException::new
                                );
                            });
                    });
            })
            .POST("", contentType(MediaType.APPLICATION_FORM_URLENCODED),
                request -> {
                    // get username and email
                    return request.formData()
                        .flatMap(formData -> {
                            var locale = Optional.ofNullable(
                                    request.exchange().getLocaleContext().getLocale()
                                )
                                .orElseGet(Locale::getDefault);
                            var email = formData.getFirst("email");
                            if (StringUtils.isBlank(email)) {
                                var error = messageSource.getMessage(
                                    "passwordReset.email.blank",
                                    null,
                                    "Email can't be blank",
                                    locale
                                );
                                return ServerResponse.ok().render(SEND_TEMPLATE, Map.of(
                                    "error", error
                                ));
                            }
                            return emailPasswordRecoveryService.sendPasswordResetEmail(email)
                                .then(ServerResponse.ok().render(SEND_TEMPLATE, Map.of(
                                    "sent", true
                                )))
                                .transformDeferred(rateLimiterForSendPasswordResetEmail(
                                    request.exchange().getRequest()
                                ))
                                .onErrorMap(
                                    RequestNotPermitted.class, RateLimitExceededException::new
                                );
                        });
                })
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
}
