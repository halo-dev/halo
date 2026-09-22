package run.halo.app.security.sudo;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.BiFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.exception.AccessDeniedException;
import run.halo.app.infra.exception.RateLimitExceededException;
import run.halo.app.infra.exception.UserNotFoundException;

/**
 * Session-scoped sudo confirmation.
 *
 * @author johnniang
 * @since 2.27.0
 */
@Slf4j
@Component
class SudoService {

    static final String SESSION_ATTRIBUTE = "halo.sudo.expiresAt";
    static final Duration SUDO_TTL = Duration.ofMinutes(30);
    static final String SEND_CODE_RATE_LIMITER_CONFIG = "send-sudo-code";
    static final String CONFIRM_RATE_LIMITER_CONFIG = "sudo-confirm";

    private final List<SudoVerificationProvider> providers;
    private final UserService userService;
    private final RateLimiterRegistry rateLimiterRegistry;

    private Clock clock = Clock.systemUTC();

    SudoService(
            List<SudoVerificationProvider> providers,
            UserService userService,
            RateLimiterRegistry rateLimiterRegistry) {
        this.providers = providers;
        this.userService = userService;
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    void setClock(Clock clock) {
        Assert.notNull(clock, "Clock must not be null");
        this.clock = clock;
    }

    Mono<SudoStatus> status(ServerWebExchange exchange) {
        return requireSessionAuthentication()
                .flatMap(auth -> Mono.zip(
                        availableMethods(auth.getName()),
                        expiresAt(exchange).defaultIfEmpty(Instant.EPOCH),
                        (methods, expiresAt) -> {
                            var active = expiresAt.isAfter(clock.instant());
                            return new SudoStatus(active, active ? expiresAt : null, methods);
                        }));
    }

    Mono<Void> sendCode(String method) {
        return requireSessionAuthentication()
                .flatMap(auth -> invoke(
                        auth.getName(),
                        SEND_CODE_RATE_LIMITER_CONFIG,
                        method,
                        (user, provider) -> provider.sendCode(user)));
    }

    Mono<Void> confirm(String method, String code, ServerWebExchange exchange) {
        return requireSessionAuthentication()
                .flatMap(auth -> invoke(
                                auth.getName(),
                                CONFIRM_RATE_LIMITER_CONFIG,
                                method,
                                (user, provider) -> provider.verify(user, code))
                        .then(Mono.defer(() -> activate(exchange)))
                        .doOnSuccess(
                                unused -> log.info("Sudo confirmed for user '{}' via '{}'", auth.getName(), method))
                        .doOnError(error ->
                                log.info("Sudo confirmation failed for user '{}' via '{}'", auth.getName(), method)));
    }

    /**
     * Applies the method rate limiter, resolves the user, and runs the provider action for the requested method. Both
     * {@link #sendCode(String)} and {@link #confirm(String, String, ServerWebExchange)} share this pipeline so that
     * every method is throttled and resolved identically.
     */
    private <T> Mono<T> invoke(
            String username,
            String rateLimiterConfig,
            String method,
            BiFunction<User, SudoVerificationProvider, Mono<T>> action) {
        return Mono.just(username)
                .transformDeferred(rateLimiter(username, rateLimiterConfig))
                .flatMap(userService::getUser)
                .flatMap(user -> requireSupported(user, method)
                        .flatMap(provider -> Mono.defer(() -> action.apply(user, provider))))
                .onErrorMap(RequestNotPermitted.class, RateLimitExceededException::new);
    }

    Mono<Void> requireSudo(ServerWebExchange exchange, String username) {
        return isActive(exchange).flatMap(active -> {
            if (active) {
                return Mono.empty();
            }
            return availableMethods(username)
                    .onErrorResume(UserNotFoundException.class, ignored -> Mono.just(List.of()))
                    .flatMap(methods -> {
                        if (methods.isEmpty()) {
                            return Mono.empty();
                        }
                        var names = methods.stream().map(SudoMethod::name).toList();
                        return Mono.error(new SudoRequiredException(names));
                    });
        });
    }

    Mono<Boolean> isActive(ServerWebExchange exchange) {
        return expiresAt(exchange)
                .map(expiresAt -> expiresAt.isAfter(clock.instant()))
                .defaultIfEmpty(false);
    }

    Mono<List<SudoMethod>> availableMethods(String username) {
        return userService.getUser(username).flatMapMany(this::availableMethods).collectList();
    }

    Flux<SudoMethod> availableMethods(User user) {
        return supportedProviders(user)
                .map(provider ->
                        new SudoMethod(provider.method(), provider.canSendCode(), provider.maskedTarget(user)));
    }

    private Flux<SudoVerificationProvider> supportedProviders(User user) {
        return Flux.fromIterable(providers).filterWhen(provider -> provider.supports(user));
    }

    Mono<Authentication> requireSessionAuthentication() {
        return ReactiveSecurityContextHolder.getContext()
                .mapNotNull(SecurityContext::getAuthentication)
                .filter(SudoService::isSessionAuthentication)
                .switchIfEmpty(Mono.error(() -> new AccessDeniedException("Sudo APIs require a browser session")));
    }

    static boolean isJwtAuthentication(Authentication authentication) {
        return authentication instanceof JwtAuthenticationToken;
    }

    static boolean isSessionAuthentication(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && !isJwtAuthentication(authentication);
    }

    private <T> RateLimiterOperator<T> rateLimiter(String username, String configName) {
        var key = configName + "-" + username;
        return RateLimiterOperator.of(rateLimiterRegistry.rateLimiter(key, configName));
    }

    private Mono<SudoVerificationProvider> requireSupported(User user, String method) {
        return supportedProviders(user)
                .filter(provider -> provider.method().equals(method))
                .next()
                .switchIfEmpty(Mono.error(() -> new ServerWebInputException("Sudo method is not available")));
    }

    private Mono<Void> activate(ServerWebExchange exchange) {
        var expiresAt = clock.instant().plus(SUDO_TTL);
        return exchange.getSession()
                .doOnNext(session -> session.getAttributes().put(SESSION_ATTRIBUTE, expiresAt))
                .then();
    }

    private Mono<Instant> expiresAt(ServerWebExchange exchange) {
        return exchange.getSession()
                .mapNotNull(session -> session.getAttribute(SESSION_ATTRIBUTE))
                .filter(Instant.class::isInstance)
                .cast(Instant.class);
    }
}
