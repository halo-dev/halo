package run.halo.app.security.authentication.login;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.ObservationReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
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

    private final ObservationRegistry observationRegistry;

    private final ReactiveUserDetailsService userDetailsService;

    private final ReactiveUserDetailsPasswordService passwordService;

    private final PasswordEncoder passwordEncoder;

    private final ServerSecurityContextRepository securityContextRepository;

    private final CryptoService cryptoService;

    private final AuthenticationWebFilter authenticationWebFilter;

    private final ExtensionGetter extensionGetter;

    public UsernamePasswordAuthenticator(ServerResponse.Context context,
        ObservationRegistry observationRegistry, ReactiveUserDetailsService userDetailsService,
        ReactiveUserDetailsPasswordService passwordService, PasswordEncoder passwordEncoder,
        ServerSecurityContextRepository securityContextRepository, CryptoService cryptoService,
        RateLimiterRegistry rateLimiterRegistry, MessageSource messageSource,
        ExtensionGetter extensionGetter) {
        this.observationRegistry = observationRegistry;
        this.userDetailsService = userDetailsService;
        this.passwordService = passwordService;
        this.passwordEncoder = passwordEncoder;
        this.securityContextRepository = securityContextRepository;
        this.cryptoService = cryptoService;
        this.extensionGetter = extensionGetter;

        this.authenticationWebFilter = new AuthenticationWebFilter(authenticationManager());
        configureAuthenticationWebFilter(this.authenticationWebFilter, context, messageSource,
            rateLimiterRegistry);
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

    void configureAuthenticationWebFilter(AuthenticationWebFilter filter,
        ServerResponse.Context context,
        MessageSource messageSource,
        RateLimiterRegistry rateLimiterRegistry) {
        var requiresMatcher = ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/login");
        var handler = new UsernamePasswordHandler(context, messageSource);
        var authConverter = new LoginAuthenticationConverter(cryptoService, rateLimiterRegistry);
        filter.setRequiresAuthenticationMatcher(requiresMatcher);
        filter.setAuthenticationFailureHandler(handler);
        filter.setAuthenticationSuccessHandler(handler);
        filter.setServerAuthenticationConverter(authConverter);
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

}
