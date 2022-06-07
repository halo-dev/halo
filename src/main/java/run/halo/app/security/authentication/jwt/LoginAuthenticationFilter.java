package run.halo.app.security.authentication.jwt;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.infra.properties.JwtProperties;

public class LoginAuthenticationFilter extends AuthenticationWebFilter {

    public LoginAuthenticationFilter(LoginAuthenticationManager authenticationManager,
        CodecConfigurer codec,
        JwtEncoder jwtEncoder,
        JwtProperties jwtProp,
        ServerResponse.Context responseContext) {
        super(authenticationManager);

        var loginMatcher = new AndServerWebExchangeMatcher(
            pathMatchers(HttpMethod.POST, "/api/auth/token"),
            new MediaTypeServerWebExchangeMatcher(MediaType.APPLICATION_JSON)
        );

        setRequiresAuthenticationMatcher(loginMatcher);
        setServerAuthenticationConverter(new LoginAuthenticationConverter(codec.getReaders()));
        setAuthenticationSuccessHandler(
            new LoginAuthenticationSuccessHandler(jwtEncoder, jwtProp, responseContext));
        setAuthenticationFailureHandler(new LoginAuthenticationFailureHandler(responseContext));
    }
}
