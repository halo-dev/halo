package run.halo.app.core.endpoint.uc;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.GroupVersion;
import run.halo.app.infra.exception.UnsatisfiedAttributeValueException;
import run.halo.app.security.verification.SecurityVerificationRequiredException;
import run.halo.app.security.verification.SecurityVerificationService;

/**
 * User endpoint for UC (User Center). It provides APIs for the current user to get their profile information and to set
 * or change their password.
 *
 * @author JohnNiang
 * @since 2.26.0
 */
@Component
@RequiredArgsConstructor
class UcUserEndpoint implements CustomEndpoint {

    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

    private final UserService userService;

    private final SecurityVerificationService securityVerificationService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "UserV1alpha1Uc";
        return SpringdocRouteBuilder.route()
                .GET(
                        "/users/-",
                        this::getCurrentUser,
                        builder -> builder.operationId("GetMyUser")
                                .tag(tag)
                                .description("Get current user profile without password hash.")
                                .response(responseBuilder().implementation(UcUserVo.class)))
                .PUT(
                        "/users/-/password",
                        this::changeMyPassword,
                        builder -> builder.operationId("ChangeMyPassword")
                                .tag(tag)
                                .description("Set or change the password of the current user.")
                                .requestBody(requestBodyBuilder()
                                        .required(true)
                                        .implementation(ChangeMyPasswordRequest.class))
                                .response(responseBuilder().implementation(UcUserVo.class)))
                .build();
    }

    private Mono<ServerResponse> getCurrentUser(ServerRequest request) {
        return authenticated()
                .map(Authentication::getName)
                .flatMap(userService::getUser)
                .flatMap(user -> request.exchange().getSession().map(session -> toUserVo(user, session)))
                .flatMap(userVo -> ServerResponse.ok().bodyValue(userVo));
    }

    private Mono<ServerResponse> changeMyPassword(ServerRequest request) {
        return authenticated()
                .map(Authentication::getName)
                .flatMap(username -> request.bodyToMono(ChangeMyPasswordRequest.class)
                        .switchIfEmpty(
                                Mono.defer(() -> Mono.error(new ServerWebInputException("Request body is empty"))))
                        .flatMap(changeRequest -> userService.getUser(username).flatMap(user -> {
                            var passwordSet = StringUtils.hasText(user.getSpec().getPassword());
                            return requirePasswordChangeVerification(user, request)
                                    .then(Mono.defer(() -> {
                                        var verifyPassword = passwordSet
                                                ? verifyOldPassword(username, changeRequest.oldPassword())
                                                : Mono.just(true);
                                        return verifyPassword
                                                .flatMap(ignored -> userService.updateWithRawPassword(
                                                        username, changeRequest.password()))
                                                .defaultIfEmpty(user)
                                                .flatMap(u -> request.exchange()
                                                        .getSession()
                                                        .map(session -> toUserVo(u, session)));
                                    }));
                        })))
                .flatMap(userVo -> ServerResponse.ok().bodyValue(userVo));
    }

    private Mono<Boolean> verifyOldPassword(String username, String oldPassword) {
        if (!StringUtils.hasText(oldPassword)) {
            return Mono.error(new UnsatisfiedAttributeValueException(
                    "Old password is required.", "problemDetail.user.oldPassword.required", null));
        }
        return userService
                .confirmPassword(username, oldPassword)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new UnsatisfiedAttributeValueException(
                        "Old password is incorrect.", "problemDetail.user.oldPassword.notMatch", null)));
    }

    private Mono<Void> requirePasswordChangeVerification(User user, ServerRequest request) {
        return request.exchange().getSession().flatMap(session -> {
            var passwordSet = StringUtils.hasText(user.getSpec().getPassword());
            if (passwordSet
                    && securityVerificationService.isAvailable(user)
                    && !securityVerificationService.isVerified(session)) {
                return Mono.error(new SecurityVerificationRequiredException());
            }
            return Mono.empty();
        });
    }

    private UcUserVo toUserVo(User user, WebSession session) {
        var passwordSet = StringUtils.hasText(user.getSpec().getPassword());
        var verificationRequired = passwordSet
                && securityVerificationService.isAvailable(user)
                && !securityVerificationService.isVerified(session);
        return new UcUserVo(
                user.getMetadata().getName(),
                user.getSpec().getDisplayName(),
                user.getSpec().getAvatar(),
                passwordSet,
                verificationRequired);
    }

    private Mono<Authentication> authenticated() {
        return ReactiveSecurityContextHolder.getContext()
                .mapNotNull(SecurityContext::getAuthentication)
                .filter(trustResolver::isAuthenticated)
                .switchIfEmpty(Mono.error(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Anonymous user is not allowed to access user profile.")));
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("uc.api.halo.run/v1alpha1");
    }

    /**
     * Masked current user profile. The password hash is never exposed.
     *
     * @param name username of the current user
     * @param displayName display name of the current user
     * @param avatar avatar of the current user
     * @param passwordSet whether the current user has a password set
     */
    record UcUserVo(
            @Schema(requiredMode = REQUIRED) String name,
            String displayName,
            String avatar,
            @Schema(requiredMode = REQUIRED) boolean passwordSet,
            @Schema(requiredMode = REQUIRED) boolean passwordChangeVerificationRequired) {}

    /**
     * Payload for setting or changing the current user's password.
     *
     * @param oldPassword old password, required and verified only when the current user has a password; ignored
     *     otherwise
     * @param password new password of the current user
     */
    record ChangeMyPasswordRequest(
            @Schema(
                    description = "Old password. Required and verified only when the current user "
                            + "has a password; ignored otherwise.")
            String oldPassword,

            @Schema(requiredMode = REQUIRED, minLength = 5, maxLength = 257)
            String password) {

        public ChangeMyPasswordRequest {
            if (password == null || password.length() < 5 || password.length() > 257) {
                throw new UnsatisfiedAttributeValueException(
                        "password is required.", "validation.error.password.size", new Object[] {5, 257});
            }
        }
    }
}
