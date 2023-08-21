package run.halo.app.security.authentication.login;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static run.halo.app.infra.exception.Exceptions.createErrorResponse;
import static run.halo.app.security.authentication.WebExchangeMatchers.ignoringMediaTypeAll;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.ObservationReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.infra.exception.RateLimitExceededException;
import run.halo.app.infra.utils.IpAddressUtils;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.security.AdditionalWebFilter;

/**
 * Authentication filter for username and password.
 *
 * @author guqing
 * @since 2.4.0
 */
@Slf4j
@Component
public class UsernamePasswordAuthenticator implements AdditionalWebFilter {

    private final ServerResponse.Context context;

    private final ObservationRegistry observationRegistry;

    private final ReactiveUserDetailsService userDetailsService;

    private final ReactiveUserDetailsPasswordService passwordService;

    private final PasswordEncoder passwordEncoder;

    private final ServerSecurityContextRepository securityContextRepository;

    private final CryptoService cryptoService;

    private final AuthenticationWebFilter authenticationWebFilter;

    private final RateLimiterRegistry rateLimiterRegistry;
    private final MessageSource messageSource;

    private final ExtensionGetter extensionGetter;

    public UsernamePasswordAuthenticator(ServerResponse.Context context,
        ObservationRegistry observationRegistry, ReactiveUserDetailsService userDetailsService,
        ReactiveUserDetailsPasswordService passwordService, PasswordEncoder passwordEncoder,
        ServerSecurityContextRepository securityContextRepository, CryptoService cryptoService,
        RateLimiterRegistry rateLimiterRegistry, MessageSource messageSource,
        ExtensionGetter extensionGetter) {
        this.context = context;
        this.observationRegistry = observationRegistry;
        this.userDetailsService = userDetailsService;
        this.passwordService = passwordService;
        this.passwordEncoder = passwordEncoder;
        this.securityContextRepository = securityContextRepository;
        this.cryptoService = cryptoService;
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.messageSource = messageSource;
        this.extensionGetter = extensionGetter;

        this.authenticationWebFilter =
            new UsernamePasswordAuthenticationWebFilter(authenticationManager());
        configureAuthenticationWebFilter(this.authenticationWebFilter);
    }

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return authenticationWebFilter.filter(exchange, chain);
    }

    @Override
    public int getOrder() {
        return SecurityWebFiltersOrder.FORM_LOGIN.getOrder();
    }

    void configureAuthenticationWebFilter(AuthenticationWebFilter filter) {
        var requiresMatcher = ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/login");
        filter.setRequiresAuthenticationMatcher(requiresMatcher);
        filter.setAuthenticationFailureHandler(new LoginFailureHandler());
        filter.setAuthenticationSuccessHandler(new LoginSuccessHandler());
        filter.setServerAuthenticationConverter(new LoginAuthenticationConverter(cryptoService
        ));
        filter.setSecurityContextRepository(securityContextRepository);
    }

    ReactiveAuthenticationManager authenticationManager() {
        var manager = new UsernamePasswordDelegatingAuthenticationManager(extensionGetter,
            defaultAuthenticationManager());
        return new ObservationReactiveAuthenticationManager(observationRegistry, manager);
    }

    ReactiveAuthenticationManager defaultAuthenticationManager() {
        var manager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        manager.setPasswordEncoder(passwordEncoder);
        manager.setUserDetailsPasswordService(passwordService);
        return manager;
    }

    private <T> RateLimiterOperator<T> createIPBasedRateLimiter(ServerWebExchange exchange) {
        var clientIp = IpAddressUtils.getClientIp(exchange.getRequest());
        var rateLimiter =
            rateLimiterRegistry.rateLimiter("authentication-from-ip-" + clientIp,
                "authentication");
        if (log.isDebugEnabled()) {
            var metrics = rateLimiter.getMetrics();
            log.debug(
                "Authentication with Rate Limiter: {}, available permissions: {}, number of "
                    + "waiting threads: {}",
                rateLimiter, metrics.getAvailablePermissions(),
                metrics.getNumberOfWaitingThreads());
        }
        return RateLimiterOperator.of(rateLimiter);
    }

    private Mono<Void> handleRateLimitExceededException(RateLimitExceededException e,
        ServerWebExchange exchange) {
        var errorResponse = createErrorResponse(e, null, exchange, messageSource);
        return writeErrorResponse(errorResponse, exchange);
    }

    private Mono<Void> handleAuthenticationException(AuthenticationException exception,
        ServerWebExchange exchange) {
        var errorResponse = createErrorResponse(exception, UNAUTHORIZED, exchange, messageSource);
        return writeErrorResponse(errorResponse, exchange);
    }

    private Mono<Void> writeErrorResponse(ErrorResponse errorResponse,
        ServerWebExchange exchange) {
        return ServerResponse.status(errorResponse.getStatusCode())
            .contentType(APPLICATION_JSON)
            .bodyValue(errorResponse.getBody())
            .flatMap(response -> response.writeTo(exchange, context));
    }

    private class UsernamePasswordAuthenticationWebFilter extends AuthenticationWebFilter {

        public UsernamePasswordAuthenticationWebFilter(
            ReactiveAuthenticationManager authenticationManager) {
            super(authenticationManager);
        }

        @Override
        protected Mono<Void> onAuthenticationSuccess(Authentication authentication,
            WebFilterExchange webFilterExchange) {
            return super.onAuthenticationSuccess(authentication, webFilterExchange)
                .transformDeferred(createIPBasedRateLimiter(webFilterExchange.getExchange()))
                .onErrorMap(RequestNotPermitted.class, RateLimitExceededException::new)
                .onErrorResume(RateLimitExceededException.class,
                    e -> handleRateLimitExceededException(e, webFilterExchange.getExchange()));
        }
    }

    public class LoginSuccessHandler implements ServerAuthenticationSuccessHandler {

        private final ServerAuthenticationSuccessHandler defaultHandler =
            new RedirectServerAuthenticationSuccessHandler("/console/");

        @Override
        public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange,
            Authentication authentication) {
            var exchange = webFilterExchange.getExchange();
            return ignoringMediaTypeAll(APPLICATION_JSON)
                .matches(exchange)
                .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                .switchIfEmpty(
                    defaultHandler.onAuthenticationSuccess(webFilterExchange, authentication)
                        .then(Mono.empty())
                )
                .flatMap(matchResult -> {
                    var principal = authentication.getPrincipal();
                    if (principal instanceof CredentialsContainer credentialsContainer) {
                        credentialsContainer.eraseCredentials();
                    }

                    return ServerResponse.ok()
                        .contentType(APPLICATION_JSON)
                        .bodyValue(principal)
                        .flatMap(serverResponse ->
                            serverResponse.writeTo(exchange, context));
                });
        }
    }

    /**
     * Handles login failure.
     *
     * @author johnniang
     */
    public class LoginFailureHandler implements ServerAuthenticationFailureHandler {

        private final ServerAuthenticationFailureHandler defaultHandler =
            new RedirectServerAuthenticationFailureHandler("/console?error#/login");

        public LoginFailureHandler() {
        }

        @Override
        public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange,
            AuthenticationException exception) {
            var exchange = webFilterExchange.getExchange();
            return ignoringMediaTypeAll(APPLICATION_JSON)
                .matches(exchange)
                .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                .switchIfEmpty(defaultHandler.onAuthenticationFailure(webFilterExchange, exception)
                    // Skip the handleAuthenticationException.
                    .then(Mono.empty())
                )
                .flatMap(matchResult -> handleAuthenticationException(exception, exchange))
                .transformDeferred(createIPBasedRateLimiter(exchange))
                .onErrorMap(RequestNotPermitted.class, RateLimitExceededException::new)
                .onErrorResume(RateLimitExceededException.class,
                    e -> handleRateLimitExceededException(e, exchange));
        }

    }
}
