package run.halo.app.security.authentication.login;

import static run.halo.app.security.authentication.WebExchangeMatchers.ignoringMediaTypeAll;

import io.micrometer.observation.ObservationRegistry;
import java.util.Map;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.security.AdditionalWebFilter;

/**
 * Authentication filter for username and password.
 *
 * @author guqing
 * @since 2.4.0
 */
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

    public UsernamePasswordAuthenticator(ServerResponse.Context context,
        ObservationRegistry observationRegistry, ReactiveUserDetailsService userDetailsService,
        ReactiveUserDetailsPasswordService passwordService, PasswordEncoder passwordEncoder,
        ServerSecurityContextRepository securityContextRepository, CryptoService cryptoService) {
        this.context = context;
        this.observationRegistry = observationRegistry;
        this.userDetailsService = userDetailsService;
        this.passwordService = passwordService;
        this.passwordEncoder = passwordEncoder;
        this.securityContextRepository = securityContextRepository;
        this.cryptoService = cryptoService;

        this.authenticationWebFilter = new AuthenticationWebFilter(authenticationManager());
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
        filter.setServerAuthenticationConverter(new LoginAuthenticationConverter(cryptoService));
        filter.setSecurityContextRepository(securityContextRepository);
    }

    ReactiveAuthenticationManager authenticationManager() {
        var manager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        manager.setPasswordEncoder(passwordEncoder);
        manager.setUserDetailsPasswordService(passwordService);
        return new ObservationReactiveAuthenticationManager(observationRegistry, manager);
    }

    public class LoginSuccessHandler implements ServerAuthenticationSuccessHandler {

        private final ServerAuthenticationSuccessHandler defaultHandler =
            new RedirectServerAuthenticationSuccessHandler("/console/");

        @Override
        public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange,
            Authentication authentication) {
            return ignoringMediaTypeAll(MediaType.APPLICATION_JSON)
                .matches(webFilterExchange.getExchange())
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(principal)
                        .flatMap(serverResponse ->
                            serverResponse.writeTo(webFilterExchange.getExchange(), context));
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

        @Override
        public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange,
            AuthenticationException exception) {
            return ignoringMediaTypeAll(MediaType.APPLICATION_JSON).matches(
                    webFilterExchange.getExchange())
                .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                .flatMap(matchResult -> ServerResponse.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                        "error", exception.getLocalizedMessage()
                    ))
                    .flatMap(serverResponse -> serverResponse.writeTo(
                        webFilterExchange.getExchange(), context)))
                .switchIfEmpty(
                    defaultHandler.onAuthenticationFailure(webFilterExchange, exception));
        }
    }
}
