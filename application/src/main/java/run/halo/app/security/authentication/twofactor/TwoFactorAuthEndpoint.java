package run.halo.app.security.authentication.twofactor;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import lombok.Data;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.ValidationUtils;
import run.halo.app.infra.exception.AccessDeniedException;
import run.halo.app.infra.exception.RequestBodyValidationException;
import run.halo.app.security.authentication.twofactor.totp.TotpAuthService;

@Component
public class TwoFactorAuthEndpoint implements CustomEndpoint {

    private final ReactiveExtensionClient client;

    private final UserService userService;

    private final TotpAuthService totpAuthService;

    private final Validator validator;

    private final PasswordEncoder passwordEncoder;

    private final ExternalUrlSupplier externalUrl;

    public TwoFactorAuthEndpoint(ReactiveExtensionClient client,
        UserService userService,
        TotpAuthService totpAuthService,
        Validator validator,
        PasswordEncoder passwordEncoder,
        ExternalUrlSupplier externalUrl) {
        this.client = client;
        this.userService = userService;
        this.totpAuthService = totpAuthService;
        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
        this.externalUrl = externalUrl;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "TwoFactorAuthV1alpha1Uc";
        return route().nest(path("/authentications/two-factor"),
            () -> route()
                .GET("/settings", this::getTwoFactorSettings,
                    builder -> builder.operationId("GetTwoFactorAuthenticationSettings")
                        .tag(tag)
                        .description("Get Two-factor authentication settings.")
                        .response(responseBuilder().implementation(TwoFactorAuthSettings.class)))
                .PUT("/settings/enabled", this::enableTwoFactor,
                    builder -> builder.operationId("EnableTwoFactor")
                        .tag(tag)
                        .description("Enable Two-factor authentication")
                        .requestBody(requestBodyBuilder().implementation(PasswordRequest.class))
                        .response(responseBuilder().implementation(TwoFactorAuthSettings.class)))
                .PUT("/settings/disabled", this::disableTwoFactor,
                    builder -> builder.operationId("DisableTwoFactor")
                        .tag(tag)
                        .description("Disable Two-factor authentication")
                        .requestBody(requestBodyBuilder().implementation(PasswordRequest.class))
                        .response(responseBuilder().implementation(TwoFactorAuthSettings.class)))
                .POST("/totp", this::configureTotp,
                    builder -> builder.operationId("ConfigurerTotp")
                        .tag(tag)
                        .description("Configure a TOTP")
                        .requestBody(requestBodyBuilder().implementation(TotpRequest.class))
                        .response(responseBuilder().implementation(TwoFactorAuthSettings.class)))
                .DELETE("/totp/-", this::deleteTotp,
                    builder -> builder.operationId("DeleteTotp")
                        .tag(tag)
                        .requestBody(requestBodyBuilder().implementation(PasswordRequest.class))
                        .response(responseBuilder().implementation(TwoFactorAuthSettings.class)))
                .GET("/totp/auth-link", this::getTotpAuthLink,
                    builder -> builder.operationId("GetTotpAuthLink")
                        .tag(tag)
                        .description("Get TOTP auth link, including secret")
                        .response(responseBuilder().implementation(TotpAuthLinkResponse.class)))
                .build(),
            builder -> builder.description("Two-factor authentication endpoint(User-scoped)")
        ).build();
    }

    private Mono<ServerResponse> deleteTotp(ServerRequest request) {
        var totpDeleteRequestMono = request.bodyToMono(PasswordRequest.class)
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException("Request body required")))
            .doOnNext(passwordRequest -> this.validateRequest(passwordRequest, "passwordRequest",
                request)
            );

        var twoFactorAuthSettings =
            totpDeleteRequestMono.flatMap(passwordRequest -> getCurrentUser()
                .filter(user -> {
                    var rawPassword = passwordRequest.getPassword();
                    var encodedPassword = user.getSpec().getPassword();
                    return this.passwordEncoder.matches(rawPassword, encodedPassword);
                })
                .switchIfEmpty(
                    Mono.error(() -> new ServerWebInputException("Invalid password")))
                .doOnNext(user -> {
                    var spec = user.getSpec();
                    spec.setTotpEncryptedSecret(null);
                })
                .flatMap(client::update)
                .map(TwoFactorUtils::getTwoFactorAuthSettings));
        return ServerResponse.ok().body(twoFactorAuthSettings, TwoFactorAuthSettings.class);
    }

    @Data
    public static class PasswordRequest {

        @NotBlank
        private String password;

    }

    private Mono<ServerResponse> disableTwoFactor(ServerRequest request) {
        return toggleTwoFactor(request, false);
    }

    private Mono<ServerResponse> enableTwoFactor(ServerRequest request) {
        return toggleTwoFactor(request, true);
    }

    private Mono<ServerResponse> toggleTwoFactor(ServerRequest request, boolean enabled) {
        return request.bodyToMono(PasswordRequest.class)
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException("Request body required")))
            .doOnNext(passwordRequest -> this.validateRequest(passwordRequest,
                "passwordRequest", request))
            .flatMap(passwordRequest -> getCurrentUser()
                .filter(user -> {
                    var encodedPassword = user.getSpec().getPassword();
                    var rawPassword = passwordRequest.getPassword();
                    return passwordEncoder.matches(rawPassword, encodedPassword);
                })
                .switchIfEmpty(
                    Mono.error(() -> new ServerWebInputException("Invalid password")))
                .doOnNext(user -> user.getSpec().setTwoFactorAuthEnabled(enabled))
                .flatMap(client::update)
                .map(TwoFactorUtils::getTwoFactorAuthSettings))
            .flatMap(twoFactorAuthSettings -> ServerResponse.ok().bodyValue(twoFactorAuthSettings));
    }

    private Mono<ServerResponse> getTotpAuthLink(ServerRequest request) {
        var authLinkResponse = getCurrentUser()
            .map(user -> {
                var username = user.getMetadata().getName();
                var url = externalUrl.getURL(request.exchange().getRequest());
                var authority = url.getAuthority();
                var authKeyId = username + ":" + authority;
                var rawSecret = totpAuthService.generateTotpSecret();
                var authLink = UriComponentsBuilder.fromUriString("otpauth://totp")
                    .path(authKeyId)
                    .queryParam("secret", rawSecret)
                    .queryParam("digits", 6)
                    .build().toUri();
                var authLinkResp = new TotpAuthLinkResponse();
                authLinkResp.setAuthLink(authLink);
                authLinkResp.setRawSecret(rawSecret);
                return authLinkResp;
            });

        return ServerResponse.ok().body(authLinkResponse, TotpAuthLinkResponse.class);
    }

    @Data
    public static class TotpAuthLinkResponse {

        /**
         * QR Code with base64 encoded.
         */
        private URI authLink;

        private String rawSecret;
    }

    private Mono<ServerResponse> configureTotp(ServerRequest request) {
        var totpRequestMono = request.bodyToMono(TotpRequest.class)
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException("Request body required.")))
            .doOnNext(totpRequest -> this.validateRequest(totpRequest, "totp", request));

        var configuredUser = totpRequestMono.flatMap(totpRequest -> {
            // validate password
            return getCurrentUser()
                .filter(user -> {
                    var encodedPassword = user.getSpec().getPassword();
                    var rawPassword = totpRequest.getPassword();
                    return passwordEncoder.matches(rawPassword, encodedPassword);
                })
                .switchIfEmpty(Mono.error(() -> new ServerWebInputException("Invalid password")))
                .doOnNext(user -> {
                    // TimeBasedOneTimePasswordUtil.
                    var rawSecret = totpRequest.getSecret();
                    int code;
                    try {
                        code = Integer.parseInt(totpRequest.getCode());
                    } catch (NumberFormatException e) {
                        throw new ServerWebInputException("Invalid code");
                    }
                    var validated = totpAuthService.validateTotp(rawSecret, code);
                    if (!validated) {
                        throw new ServerWebInputException("Invalid secret or code");
                    }
                    var encryptedSecret = totpAuthService.encryptSecret(rawSecret);
                    user.getSpec().setTotpEncryptedSecret(encryptedSecret);
                })
                .flatMap(client::update);
        });

        var twoFactorAuthSettings =
            configuredUser.map(TwoFactorUtils::getTwoFactorAuthSettings);

        return ServerResponse.ok().body(twoFactorAuthSettings, TwoFactorAuthSettings.class);
    }

    private void validateRequest(Object target, String name, ServerRequest request) {
        var bindingResult =
            ValidationUtils.validate(target, name, validator, request.exchange());
        if (bindingResult.hasErrors()) {
            throw new RequestBodyValidationException(bindingResult);
        }
    }

    @Data
    public static class TotpRequest {

        @NotBlank
        private String secret;

        @NotNull
        private String code;

        @NotBlank
        private String password;

    }

    private Mono<ServerResponse> getTwoFactorSettings(ServerRequest request) {
        return getCurrentUser()
            .map(TwoFactorUtils::getTwoFactorAuthSettings)
            .flatMap(settings -> ServerResponse.ok().bodyValue(settings));
    }

    private Mono<User> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(TwoFactorAuthEndpoint::isAuthenticatedUser)
            .switchIfEmpty(Mono.error(AccessDeniedException::new))
            .map(Authentication::getName)
            .flatMap(userService::getUser);
    }

    private static boolean isAuthenticatedUser(Authentication authentication) {
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken);
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("uc.api.security.halo.run/v1alpha1");
    }
}
