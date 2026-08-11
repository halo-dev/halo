package run.halo.app.security.verification;

import java.net.URI;
import java.util.HashMap;
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
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.EmailVerificationService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.infra.exception.AccessDeniedException;
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

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 100)
    RouterFunction<ServerResponse> securityVerificationEndpoints() {
        return RouterFunctions.route()
                .GET("/security-verification", this::renderVerificationPage)
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

    private Mono<ServerResponse> sendEmailCode(ServerRequest request) {
        return currentUser(request)
                .flatMap(user -> emailVerificationService
                        .sendSecurityVerificationCode(user.getMetadata().getName())
                        .then(ServerResponse.accepted().build()));
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
        if (StringUtils.isNotBlank(redirect) && redirect.startsWith("/") && !redirect.startsWith("//")) {
            return redirect;
        }
        return DEFAULT_REDIRECT;
    }

    private static Mono<ServerResponse> redirectTo(String location) {
        return ServerResponse.status(HttpStatus.FOUND)
                .location(URI.create(location))
                .build();
    }
}
