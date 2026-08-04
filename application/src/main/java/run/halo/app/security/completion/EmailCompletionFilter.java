package run.halo.app.security.completion;

import java.net.URI;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.security.authorization.AuthorityUtils;

/** Redirects or rejects authenticated users who must complete a verified email. */
@Slf4j
public class EmailCompletionFilter implements WebFilter {

    private static final URI EMAIL_NOT_SET_TYPE = URI.create("email-not-set");

    private static final List<String> EXEMPT_PATH_PREFIXES = List.of(
            "/oauth2",
            "/login",
            "/signup",
            "/password-reset",
            "/logout",
            "/complete-profile",
            "/system/setup",
            "/error",
            "/assets",
            "/images",
            "/js",
            "/styles",
            "/webjars",
            "/favicon.");

    private final SystemConfigFetcher systemConfigFetcher;
    private final UserService userService;
    private final ServerRequestCache requestCache;
    private final ServerResponse.Context responseContext;
    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();
    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    public EmailCompletionFilter(
            SystemConfigFetcher systemConfigFetcher,
            UserService userService,
            ServerRequestCache requestCache,
            ServerResponse.Context responseContext) {
        this.systemConfigFetcher = systemConfigFetcher;
        this.userService = userService;
        this.requestCache = requestCache;
        this.responseContext = responseContext;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .mapNotNull(SecurityContext::getAuthentication)
                .filter(this::isFullyAuthenticated)
                .flatMap(authentication -> shouldIntercept(exchange, authentication))
                .defaultIfEmpty(false)
                .flatMap(intercept -> intercept ? intercept(exchange) : chain.filter(exchange));
    }

    private boolean isFullyAuthenticated(Authentication authentication) {
        return authentication.isAuthenticated() && !trustResolver.isAnonymous(authentication);
    }

    private Mono<Boolean> shouldIntercept(ServerWebExchange exchange, Authentication authentication) {
        if (isSuperAdmin(authentication) || isExemptPath(exchange)) {
            return Mono.just(false);
        }
        return systemConfigFetcher
                .fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
                .map(SystemSetting.User::isMustVerifyEmailOnRegistration)
                .defaultIfEmpty(false)
                .flatMap(required -> {
                    if (!required) {
                        return Mono.just(false);
                    }
                    return userService
                            .getUser(authentication.getName())
                            .map(user -> !user.getSpec().isEmailVerified())
                            .doOnError(e -> log.warn(
                                    "Failed to check email verification status for user '{}'",
                                    authentication.getName(),
                                    e))
                            .onErrorReturn(false);
                });
    }

    private boolean isSuperAdmin(Authentication authentication) {
        return AuthorityUtils.authoritiesToRoles(authentication.getAuthorities())
                .contains(AuthorityUtils.SUPER_ROLE_NAME);
    }

    private boolean isExemptPath(ServerWebExchange exchange) {
        var path = exchange.getRequest().getPath().pathWithinApplication().value();
        return EXEMPT_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> intercept(ServerWebExchange exchange) {
        if (isHtmlRequest(exchange)) {
            return requestCache
                    .saveRequest(exchange)
                    .then(redirectStrategy.sendRedirect(exchange, URI.create("/complete-profile")));
        }
        return writeForbidden(exchange);
    }

    private boolean isHtmlRequest(ServerWebExchange exchange) {
        if (!HttpMethod.GET.equals(exchange.getRequest().getMethod())) {
            return false;
        }
        if (HaloUtils.isXhr(exchange.getRequest().getHeaders())) {
            return false;
        }
        return exchange.getRequest().getHeaders().getAccept().stream()
                .anyMatch(mediaType -> mediaType.includes(MediaType.TEXT_HTML));
    }

    private Mono<Void> writeForbidden(ServerWebExchange exchange) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Email is not set or verified.");
        problemDetail.setType(EMAIL_NOT_SET_TYPE);
        problemDetail.setTitle("Email Not Set");
        return ServerResponse.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyValue(problemDetail)
                .flatMap(response -> response.writeTo(exchange, responseContext));
    }
}
