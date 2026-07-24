package run.halo.app.security.authentication.emailcode;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ObservationReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.security.LoginHandlerEnhancer;
import run.halo.app.security.LoginParameterRequestCache;
import run.halo.app.security.authentication.LoginFailureHandler;
import run.halo.app.security.authentication.LoginSuccessHandler;
import run.halo.app.security.authentication.SecurityConfigurer;

/**
 * Security configurer for email verification code login at {@code POST /login/email-code}.
 *
 * <p>Unlike the password-based login, email code login does NOT trigger the two-factor authentication challenge because
 * the email code itself serves as the second factor. On success, the auth manager returns a standard
 * {@code UsernamePasswordAuthenticationToken}, which is handled by the shared {@link LoginSuccessHandler}.
 *
 * @author johnniang
 * @since 2.26.0
 */
@Component
@Order(0)
public class EmailCodeSecurityConfigurer implements SecurityConfigurer {

    private final ObservationRegistry observationRegistry;

    private final ReactiveUserDetailsService userDetailsService;

    private final EmailCodeService emailCodeService;

    private final ServerSecurityContextRepository securityContextRepository;

    private final ServerResponse.Context context;

    private final MessageSource messageSource;

    private final LoginHandlerEnhancer loginHandlerEnhancer;

    private final LoginParameterRequestCache parameterRequestCache;

    private final RateLimiterRegistry rateLimiterRegistry;

    public EmailCodeSecurityConfigurer(
            ObservationRegistry observationRegistry,
            ReactiveUserDetailsService userDetailsService,
            EmailCodeService emailCodeService,
            ServerSecurityContextRepository securityContextRepository,
            ServerResponse.Context context,
            MessageSource messageSource,
            LoginHandlerEnhancer loginHandlerEnhancer,
            LoginParameterRequestCache parameterRequestCache,
            RateLimiterRegistry rateLimiterRegistry) {
        this.observationRegistry = observationRegistry;
        this.userDetailsService = userDetailsService;
        this.emailCodeService = emailCodeService;
        this.securityContextRepository = securityContextRepository;
        this.context = context;
        this.messageSource = messageSource;
        this.loginHandlerEnhancer = loginHandlerEnhancer;
        this.parameterRequestCache = parameterRequestCache;
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @Override
    public void configure(ServerHttpSecurity http) {
        var authConverter = new EmailCodeLoginAuthenticationConverter(rateLimiterRegistry);
        var filter = new AuthenticationWebFilter(authenticationManager());
        var requiresMatcher = ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/login/email-code");
        filter.setRequiresAuthenticationMatcher(requiresMatcher);
        filter.setAuthenticationSuccessHandler(
                new LoginSuccessHandler(context, loginHandlerEnhancer, parameterRequestCache));
        filter.setAuthenticationFailureHandler(
                new LoginFailureHandler("email-code", context, messageSource, loginHandlerEnhancer));
        filter.setServerAuthenticationConverter(authConverter);
        filter.setSecurityContextRepository(securityContextRepository);

        http.addFilterAt(filter, SecurityWebFiltersOrder.FORM_LOGIN);
    }

    ReactiveAuthenticationManager authenticationManager() {
        var manager = new EmailCodeReactiveAuthenticationManager(emailCodeService, userDetailsService);
        return new ObservationReactiveAuthenticationManager(observationRegistry, manager);
    }
}
