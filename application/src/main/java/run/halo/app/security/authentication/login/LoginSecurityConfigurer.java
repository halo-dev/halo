package run.halo.app.security.authentication.login;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ObservationReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.security.authentication.CryptoService;
import run.halo.app.security.authentication.SecurityConfigurer;
import run.halo.app.security.authentication.rememberme.RememberMeServices;

@Component
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

    private final RememberMeServices rememberMeServices;

    public LoginSecurityConfigurer(ObservationRegistry observationRegistry,
        ReactiveUserDetailsService userDetailsService,
        ReactiveUserDetailsPasswordService passwordService, PasswordEncoder passwordEncoder,
        ServerSecurityContextRepository securityContextRepository, CryptoService cryptoService,
        ExtensionGetter extensionGetter, ServerResponse.Context context,
        MessageSource messageSource, RateLimiterRegistry rateLimiterRegistry,
        RememberMeServices rememberMeServices) {
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
        this.rememberMeServices = rememberMeServices;
    }

    @Override
    public void configure(ServerHttpSecurity http) {
        var filter = new AuthenticationWebFilter(authenticationManager());
        var requiresMatcher = ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/login");
        var handler = new UsernamePasswordHandler(context, messageSource, rememberMeServices);
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
