package run.halo.app.theme.endpoint;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.service.EmailPasswordRecoveryService;
import run.halo.app.core.extension.service.EmailVerificationService;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.GroupVersion;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.ValidationUtils;
import run.halo.app.infra.exception.AccessDeniedException;
import run.halo.app.infra.exception.EmailVerificationFailed;
import run.halo.app.infra.exception.RateLimitExceededException;
import run.halo.app.infra.utils.IpAddressUtils;

/**
 * User endpoint for unauthenticated user.
 *
 * @author guqing
 * @since 2.4.0
 */
@Component
@RequiredArgsConstructor
public class PublicUserEndpoint implements CustomEndpoint {
    private final UserService userService;
    private final ServerSecurityContextRepository securityContextRepository;
    private final ReactiveUserDetailsService reactiveUserDetailsService;
    private final EmailPasswordRecoveryService emailPasswordRecoveryService;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final SystemConfigurableEnvironmentFetcher environmentFetcher;
    private final EmailVerificationService emailVerificationService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "UserV1alpha1Public";
        return SpringdocRouteBuilder.route()
            .POST("/users/-/signup", this::signUp,
                builder -> builder.operationId("SignUp")
                    .description("Sign up a new user")
                    .tag(tag)
                    .requestBody(requestBodyBuilder().required(true)
                        .implementation(SignUpRequest.class)
                    )
                    .response(responseBuilder().implementation(User.class))
            )
            .POST("/users/-/send-register-verify-email", this::sendRegisterVerifyEmail,
                builder -> builder.operationId("SendRegisterVerifyEmail")
                    .description(
                        "Send registration verification email, which can be called when "
                            + "mustVerifyEmailOnRegistration in user settings is true"
                    )
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .implementation(RegisterVerifyEmailRequest.class)
                    )
                    .response(responseBuilder()
                        .responseCode(HttpStatus.NO_CONTENT.toString())
                        .implementation(Void.class)
                    )
            )
            .POST("/users/-/send-password-reset-email", this::sendPasswordResetEmail,
                builder -> builder.operationId("SendPasswordResetEmail")
                    .description("Send password reset email when forgot password")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .implementation(PasswordResetEmailRequest.class)
                    )
                    .response(responseBuilder()
                        .responseCode(HttpStatus.NO_CONTENT.toString())
                        .implementation(Void.class))
            )
            .PUT("/users/{name}/reset-password", this::resetPasswordByToken,
                builder -> builder.operationId("ResetPasswordByToken")
                    .description("Reset password by token")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .name("name")
                        .description("The name of the user")
                        .required(true)
                        .in(ParameterIn.PATH)
                    )
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .implementation(ResetPasswordRequest.class)
                    )
                    .response(responseBuilder()
                        .responseCode(HttpStatus.NO_CONTENT.toString())
                        .implementation(Void.class)
                    )
            )
            .build();
    }

    private Mono<ServerResponse> resetPasswordByToken(ServerRequest request) {
        var username = request.pathVariable("name");
        return request.bodyToMono(ResetPasswordRequest.class)
            .doOnNext(resetReq -> {
                if (StringUtils.isBlank(resetReq.token())) {
                    throw new ServerWebInputException("Token must not be blank");
                }
                if (StringUtils.isBlank(resetReq.newPassword())) {
                    throw new ServerWebInputException("New password must not be blank");
                }
            })
            .switchIfEmpty(
                Mono.error(() -> new ServerWebInputException("Request body must not be empty"))
            )
            .flatMap(resetReq -> {
                var token = resetReq.token();
                var newPassword = resetReq.newPassword();
                return emailPasswordRecoveryService.changePassword(username, newPassword, token);
            })
            .then(ServerResponse.noContent().build());
    }

    record PasswordResetEmailRequest(@Schema(requiredMode = REQUIRED) String username,
                                     @Schema(requiredMode = REQUIRED) String email) {
    }

    record ResetPasswordRequest(@Schema(requiredMode = REQUIRED, minLength = 6) String newPassword,
                                @Schema(requiredMode = REQUIRED) String token) {
    }

    record RegisterVerifyEmailRequest(@Schema(requiredMode = REQUIRED) String email) {
    }

    private Mono<ServerResponse> sendPasswordResetEmail(ServerRequest request) {
        return request.bodyToMono(PasswordResetEmailRequest.class)
            .flatMap(passwordResetRequest -> {
                var username = passwordResetRequest.username();
                var email = passwordResetRequest.email();
                return Mono.just(passwordResetRequest)
                    .transformDeferred(sendResetPasswordEmailRateLimiter(username, email))
                    .flatMap(
                        r -> emailPasswordRecoveryService.sendPasswordResetEmail(username, email))
                    .onErrorMap(RequestNotPermitted.class, RateLimitExceededException::new);
            })
            .then(ServerResponse.noContent().build());
    }

    <T> RateLimiterOperator<T> sendResetPasswordEmailRateLimiter(String username, String email) {
        String rateLimiterKey = "send-reset-password-email-" + username + ":" + email;
        var rateLimiter =
            rateLimiterRegistry.rateLimiter(rateLimiterKey, "send-reset-password-email");
        return RateLimiterOperator.of(rateLimiter);
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("api.halo.run/v1alpha1");
    }

    private Mono<ServerResponse> signUp(ServerRequest request) {
        return request.bodyToMono(SignUpRequest.class)
            .doOnNext(signUpRequest -> signUpRequest.user().getSpec().setEmailVerified(false))
            .flatMap(signUpRequest -> environmentFetcher.fetch(SystemSetting.User.GROUP,
                    SystemSetting.User.class)
                .map(user -> BooleanUtils.isTrue(user.getMustVerifyEmailOnRegistration()))
                .defaultIfEmpty(false)
                .flatMap(mustVerifyEmailOnRegistration -> {
                    if (!mustVerifyEmailOnRegistration) {
                        return Mono.just(signUpRequest);
                    }
                    if (!StringUtils.isNumeric(signUpRequest.verifyCode)) {
                        return Mono.error(new EmailVerificationFailed());
                    }
                    return emailVerificationService.verifyRegisterVerificationCode(
                            signUpRequest.user().getSpec().getEmail(),
                            signUpRequest.verifyCode)
                        .flatMap(verified -> {
                            if (BooleanUtils.isNotTrue(verified)) {
                                return Mono.error(new EmailVerificationFailed());
                            }
                            signUpRequest.user().getSpec().setEmailVerified(true);
                            return Mono.just(signUpRequest);
                        });
                })
            )
            .flatMap(signUpRequest ->
                userService.signUp(signUpRequest.user(), signUpRequest.password())
            )
            .flatMap(user -> authenticate(user.getMetadata().getName(), request.exchange())
                .thenReturn(user)
            )
            .flatMap(user -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
            )
            .transformDeferred(getRateLimiterForSignUp(request.exchange()))
            .onErrorMap(RequestNotPermitted.class, RateLimitExceededException::new);
    }

    private Mono<ServerResponse> sendRegisterVerifyEmail(ServerRequest request) {
        return request.bodyToMono(RegisterVerifyEmailRequest.class)
            .switchIfEmpty(Mono.error(
                () -> new ServerWebInputException("Required request body is missing."))
            )
            .map(emailReq -> {
                var email = emailReq.email();
                if (!ValidationUtils.isValidEmail(email)) {
                    throw new ServerWebInputException("Invalid email address.");
                }
                return email;
            })
            .flatMap(email -> environmentFetcher.fetch(SystemSetting.User.GROUP,
                    SystemSetting.User.class)
                .map(config -> BooleanUtils.isTrue(config.getMustVerifyEmailOnRegistration()))
                .defaultIfEmpty(false)
                .doOnNext(mustVerifyEmailOnRegistration -> {
                    if (!mustVerifyEmailOnRegistration) {
                        throw new AccessDeniedException("Email verification is not required.");
                    }
                })
                .transformDeferred(sendRegisterEmailVerificationCodeRateLimiter(email))
                .flatMap(s -> emailVerificationService.sendRegisterVerificationCode(email)
                    .onErrorMap(RequestNotPermitted.class, RateLimitExceededException::new))
                .onErrorMap(RequestNotPermitted.class, RateLimitExceededException::new)
            )
            .then(ServerResponse.ok().build());
    }

    private <T> RateLimiterOperator<T> getRateLimiterForSignUp(ServerWebExchange exchange) {
        var clientIp = IpAddressUtils.getClientIp(exchange.getRequest());
        var rateLimiter = rateLimiterRegistry.rateLimiter("signup-from-ip-" + clientIp,
            "signup");
        return RateLimiterOperator.of(rateLimiter);
    }

    private Mono<Void> authenticate(String username, ServerWebExchange exchange) {
        return reactiveUserDetailsService.findByUsername(username)
            .flatMap(userDetails -> {
                SecurityContextImpl securityContext = new SecurityContextImpl();
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails.getUsername(),
                        userDetails.getPassword(), userDetails.getAuthorities());
                securityContext.setAuthentication(authentication);
                return securityContextRepository.save(exchange, securityContext);
            });
    }

    private <T> RateLimiterOperator<T> sendRegisterEmailVerificationCodeRateLimiter(String email) {
        String rateLimiterKey = "send-register-verify-email:" + email;
        var rateLimiter =
            rateLimiterRegistry.rateLimiter(rateLimiterKey, "send-email-verification-code");
        return RateLimiterOperator.of(rateLimiter);
    }

    record SignUpRequest(@Schema(requiredMode = REQUIRED) User user,
                         @Schema(requiredMode = REQUIRED, minLength = 6) String password,
                         @Schema(requiredMode = NOT_REQUIRED, minLength = 6, maxLength = 6)
                         String verifyCode
    ) {
    }
}
