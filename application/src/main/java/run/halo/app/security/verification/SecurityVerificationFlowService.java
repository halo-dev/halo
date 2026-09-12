package run.halo.app.security.verification;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import java.net.URI;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.WebSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.exception.AccessDeniedException;
import run.halo.app.infra.exception.EmailVerificationFailed;

/**
 * The shared flow of a security verification attempt: resolve the current user, verify the given code against the
 * session rate limit, mark the session verified, and redirect. Public so that method-specific endpoints (community
 * built-ins or plugin-provided ones) can reuse it instead of re-implementing the session/rate-limit/redirect plumbing.
 *
 * @author JohnNiang
 * @since 2.26.0
 */
@Component
@RequiredArgsConstructor
public class SecurityVerificationFlowService {

    public static final String DEFAULT_REDIRECT = "/uc/profile";

    private final UserService userService;
    private final SecurityVerificationService securityVerificationService;
    private final RateLimiterRegistry rateLimiterRegistry;

    /** Resolve the current authenticated user, or fail with {@link AccessDeniedException}. */
    public Mono<User> currentUser(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(authentication ->
                        authentication != null && !(authentication instanceof AnonymousAuthenticationToken))
                .switchIfEmpty(Mono.error(AccessDeniedException::new))
                .map(Authentication::getName)
                .flatMap(userService::getUser);
    }

    /**
     * Verify the given code, mark the session verified on success, and redirect. Failures are mapped to the
     * verification page with the {@code error} and {@code method} query parameters so the page stays on the attempted
     * method for a retry.
     */
    public Mono<ServerResponse> verifyAndRedirect(
            ServerRequest request, String redirect, String method, Mono<Void> verifyMono) {
        return request.exchange()
                .getSession()
                .flatMap(session -> verifyMono
                        .transformDeferred(rateLimiterForVerification(session))
                        .then(Mono.fromRunnable(() -> securityVerificationService.markVerified(session)))
                        .then(redirectTo(redirect)))
                .onErrorResume(EmailVerificationFailed.class, e -> {
                    var error = "problemDetail.user.email.verify.maxAttempts".equals(e.getDetailMessageCode())
                            ? "rate-limit-exceeded"
                            : "invalid-code";
                    return redirectTo(redirectWithError(error, redirect, method));
                })
                .onErrorResume(
                        ServerWebInputException.class,
                        e -> redirectTo(redirectWithError("invalid-code", redirect, method)))
                .onErrorResume(
                        RequestNotPermitted.class,
                        e -> redirectTo(redirectWithError("rate-limit-exceeded", redirect, method)));
    }

    /** Accept only site-relative redirect targets; anything else falls back to the default. */
    public String safeRedirect(String redirect) {
        if (StringUtils.isNotBlank(redirect)
                && redirect.startsWith("/")
                && !redirect.startsWith("//")
                && !redirect.contains("\\")) {
            try {
                URI.create(redirect);
                return redirect;
            } catch (IllegalArgumentException e) {
                // fall through to default
            }
        }
        return DEFAULT_REDIRECT;
    }

    /** Build the verification page URL carrying the error and the attempted method. */
    public String redirectWithError(String error, String redirect, String method) {
        return UriComponentsBuilder.fromPath("/security-verification")
                .queryParam("error", error)
                .queryParam("redirect", redirect)
                .queryParam("method", method)
                .build()
                .encode()
                .toUriString();
    }

    public Mono<ServerResponse> redirectTo(String location) {
        return ServerResponse.status(HttpStatus.FOUND)
                .location(URI.create(location))
                .build();
    }

    private Function<Mono<Void>, Mono<Void>> rateLimiterForVerification(WebSession session) {
        // Keyed by session id like the login TOTP validation, sharing the same
        // totp-validation budget (5 attempts / 5 min). A user who rotates the
        // session (log out / in) gets a fresh budget, but re-authentication is
        // required each time, which bounds the practical attempt rate.
        var rateLimiter = rateLimiterRegistry.rateLimiter("totp-validation-" + session.getId(), "totp-validation");
        var operator = RateLimiterOperator.<Void>of(rateLimiter);
        return mono -> mono.transformDeferred(operator);
    }
}
