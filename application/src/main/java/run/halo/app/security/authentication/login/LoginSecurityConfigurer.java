package run.halo.app.security.authentication.login;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ObservationReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.security.HaloUserDetails;
import run.halo.app.security.LoginHandlerEnhancer;
import run.halo.app.security.authentication.CryptoService;
import run.halo.app.security.authentication.SecurityConfigurer;
import run.halo.app.security.authentication.twofactor.TwoFactorAuthentication;

@Component
@Order(0)
public class LoginSecurityConfigurer implements SecurityConfigurer {

    private final ObservationRegistry observationRegistry;

    private final ReactiveUserDetailsService userDetailsService;

    private final ReactiveUserDetailsPasswordService passwordService;

    private final PasswordEncoder passwordEncoder;

    private final ServerSecurityContextRepository securityContextRepository;

    private final CryptoService cryptoService;

    private final ExtensionGetter extensionGetter;
    private final ServerResponse.Context context;
    private final MessageSource messageSource;
    private final RateLimiterRegistry rateLimiterRegistry;

    private final LoginHandlerEnhancer loginHandlerEnhancer;

    public LoginSecurityConfigurer(ObservationRegistry observationRegistry,
        ReactiveUserDetailsService userDetailsService,
        ReactiveUserDetailsPasswordService passwordService, PasswordEncoder passwordEncoder,
        ServerSecurityContextRepository securityContextRepository, CryptoService cryptoService,
        ExtensionGetter extensionGetter, ServerResponse.Context context,
        MessageSource messageSource, RateLimiterRegistry rateLimiterRegistry,
        LoginHandlerEnhancer loginHandlerEnhancer) {
        this.observationRegistry = observationRegistry;
        this.userDetailsService = userDetailsService;
        this.passwordService = passwordService;
        this.passwordEncoder = passwordEncoder;
        this.securityContextRepository = securityContextRepository;
        this.cryptoService = cryptoService;
        this.extensionGetter = extensionGetter;
        this.context = context;
        this.messageSource = messageSource;
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.loginHandlerEnhancer = loginHandlerEnhancer;
    }

    @Override
    public void configure(ServerHttpSecurity http) {
        var filter = new AuthenticationWebFilter(authenticationManager()) {
            @Override
            protected Mono<Void> onAuthenticationSuccess(Authentication authentication,
                WebFilterExchange webFilterExchange) {
                // check if 2FA is enabled after authenticating successfully.
                if (authentication.getPrincipal() instanceof HaloUserDetails userDetails
                    && userDetails.isTwoFactorAuthEnabled()) {
                    authentication = new TwoFactorAuthentication(authentication);
                }
                return super.onAuthenticationSuccess(authentication, webFilterExchange);
            }
        };
        var requiresMatcher = ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/login");
        var handler = new UsernamePasswordHandler(context, messageSource, loginHandlerEnhancer);
        var authConverter = new LoginAuthenticationConverter(cryptoService, rateLimiterRegistry);
        filter.setRequiresAuthenticationMatcher(requiresMatcher);
        filter.setAuthenticationFailureHandler(handler);
        filter.setAuthenticationSuccessHandler(handler);
        filter.setServerAuthenticationConverter(authConverter);
        filter.setSecurityContextRepository(securityContextRepository);

        http.addFilterAt(filter, SecurityWebFiltersOrder.FORM_LOGIN);
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
