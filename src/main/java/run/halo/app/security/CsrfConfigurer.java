package run.halo.app.security;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfWebFilter;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import run.halo.app.security.authentication.SecurityConfigurer;

@Component
public class CsrfConfigurer implements SecurityConfigurer {

    @Override
    public void configure(ServerHttpSecurity http) {
        var csrfMatcher = new AndServerWebExchangeMatcher(
            CsrfWebFilter.DEFAULT_CSRF_MATCHER,
            new NegatedServerWebExchangeMatcher(pathMatchers("/api/**", "/apis/**")
            ));

        http.csrf().csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
            // TODO Use XorServerCsrfTokenRequestAttributeHandler instead when console implements
            // the algorithm
            .csrfTokenRequestHandler(new ServerCsrfTokenRequestAttributeHandler())
            .requireCsrfProtectionMatcher(csrfMatcher);
    }

}
