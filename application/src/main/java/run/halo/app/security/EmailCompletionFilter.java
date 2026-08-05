package run.halo.app.security;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.utils.HaloUtils;
import run.halo.app.security.authentication.WebExchangeMatchers;
import run.halo.app.security.authorization.AuthorityUtils;

@RequiredArgsConstructor
class EmailCompletionFilter implements WebFilter {

    private static final URI COMPLETE_PROFILE_URI = URI.create("/complete-profile");

    private static final String SUPER_ROLE_AUTHORITY = AuthorityUtils.ROLE_PREFIX + AuthorityUtils.SUPER_ROLE_NAME;

    private final SystemConfigFetcher systemConfigFetcher;

    private final UserService userService;

    private final ServerRequestCache requestCache;

    private final ServerResponse.Context responseContext;

    private final AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();

    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    private final ServerWebExchangeMatcher exemptMatcher = createExemptMatcher();

    private final ServerWebExchangeMatcher navigationMatcher = createNavigationMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return exemptMatcher.matches(exchange).flatMap(result -> {
            if (result.isMatch()) {
                return chain.filter(exchange);
            }
            return requiresEmailCompletion()
                    .flatMap(required -> required ? handleIncompleteUser(exchange) : chain.filter(exchange));
        });
    }

    private Mono<Boolean> requiresEmailCompletion() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .filter(authenticationTrustResolver::isAuthenticated)
                .filter(authentication -> !(authentication instanceof OAuth2AuthenticationToken))
                .filter(authentication -> !hasSuperRole(authentication))
                .flatMap(authentication -> systemConfigFetcher
                        .fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
                        .filter(SystemSetting.User::isMustVerifyEmailOnRegistration)
                        .flatMap(setting -> userService.getUser(authentication.getName()))
                        .map(user -> !user.getSpec().isEmailVerified()))
                .defaultIfEmpty(false);
    }

    private Mono<Void> handleIncompleteUser(ServerWebExchange exchange) {
        return navigationMatcher.matches(exchange).flatMap(result -> {
            if (result.isMatch() && !HaloUtils.isXhr(exchange.getRequest().getHeaders())) {
                return requestCache
                        .saveRequest(exchange)
                        .then(redirectStrategy.sendRedirect(exchange, COMPLETE_PROFILE_URI));
            }
            var problem =
                    ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "A verified email address is required.");
            problem.setType(URI.create("email-not-set"));
            return ServerResponse.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .bodyValue(problem)
                    .flatMap(response -> response.writeTo(exchange, responseContext));
        });
    }

    private static boolean hasSuperRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> SUPER_ROLE_AUTHORITY.equals(authority.getAuthority()));
    }

    private static ServerWebExchangeMatcher createExemptMatcher() {
        var paths = pathMatchers(
                "/oauth2/authorization/**",
                "/login/**",
                "/signup",
                "/password-reset/email/send",
                "/logout",
                "/complete-profile",
                "/complete-profile/send-email-code",
                "/system/setup",
                "/error",
                "/favicon.ico");
        var staticResources = pathMatchers(
                HttpMethod.GET,
                "/ui-assets/**",
                "/themes/{themeName}/assets/{*resourcePaths}",
                "/themes/{themeName}/ui-plugin/assets/{*resourcePaths}",
                "/themes/{themeName}/screenshot.{extension}",
                "/plugins/{pluginName}/assets/**",
                "/webjars/**",
                "/js/**",
                "/styles/**",
                "/halo-tracker.js",
                "/images/**");
        return new OrServerWebExchangeMatcher(paths, staticResources);
    }

    private static ServerWebExchangeMatcher createNavigationMatcher() {
        return new AndServerWebExchangeMatcher(
                pathMatchers(HttpMethod.GET, "/**"),
                WebExchangeMatchers.ignoringMediaTypeAll(MediaType.TEXT_HTML),
                new NegatedServerWebExchangeMatcher(pathMatchers("/api/**", "/apis/**", "/actuator/**")));
    }
}
