package run.halo.app.security.authentication.jwt;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.infra.properties.JwtProperties;
import run.halo.app.security.authentication.SecurityConfigurer;

/**
 * TODO: Use It after 2.0.0.
 */
public class JwtAuthenticationConfigurer implements SecurityConfigurer {

    private final ReactiveUserDetailsService userDetailsService;

    private final PasswordEncoder passwordEncoder;

    private final ServerCodecConfigurer codec;

    private final JwtEncoder jwtEncoder;

    private final ServerResponse.Context context;

    private final JwtProperties jwtProp;

    public JwtAuthenticationConfigurer(ReactiveUserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder,
        ServerCodecConfigurer codec,
        JwtEncoder jwtEncoder,
        ServerResponse.Context context,
        JwtProperties jwtProp) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.codec = codec;
        this.jwtEncoder = jwtEncoder;
        this.context = context;
        this.jwtProp = jwtProp;
    }

    @Override
    public void configure(ServerHttpSecurity http) {
        var loginManager = new LoginAuthenticationManager(userDetailsService, passwordEncoder);

        var filter = new AuthenticationWebFilter(loginManager);
        var loginMatcher = new AndServerWebExchangeMatcher(
            pathMatchers(HttpMethod.POST, "/api/auth/token"),
            new MediaTypeServerWebExchangeMatcher(MediaType.APPLICATION_JSON)
        );

        filter.setRequiresAuthenticationMatcher(loginMatcher);
        filter.setServerAuthenticationConverter(
            new LoginAuthenticationConverter(codec.getReaders()));
        filter.setAuthenticationSuccessHandler(
            new LoginAuthenticationSuccessHandler(jwtEncoder, jwtProp, context));
        filter.setAuthenticationFailureHandler(new LoginAuthenticationFailureHandler(context));

        http.addFilterAt(filter, SecurityWebFiltersOrder.FORM_LOGIN);
    }
}
