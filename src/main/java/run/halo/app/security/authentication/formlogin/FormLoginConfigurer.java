package run.halo.app.security.authentication.formlogin;

import static run.halo.app.security.authentication.WebExchangeMatchers.ignoringMediaTypeAll;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.security.authentication.SecurityConfigurer;

@Component
public class FormLoginConfigurer implements SecurityConfigurer {

    private final ServerResponse.Context context;

    public FormLoginConfigurer(ServerResponse.Context context) {
        this.context = context;
    }

    @Override
    public void configure(ServerHttpSecurity http) {
        http.formLogin()
            .authenticationSuccessHandler(new FormLoginSuccessHandler(context))
            .authenticationFailureHandler(new FormLoginFailureHandler(context))
        ;
    }

    public static class FormLoginSuccessHandler implements ServerAuthenticationSuccessHandler {

        private final ServerResponse.Context context;

        private final ServerAuthenticationSuccessHandler defaultHandler =
            new RedirectServerAuthenticationSuccessHandler("/console/");

        public FormLoginSuccessHandler(ServerResponse.Context context) {
            this.context = context;
        }

        @Override
        public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange,
            Authentication authentication) {
            return ignoringMediaTypeAll(MediaType.APPLICATION_JSON)
                .matches(webFilterExchange.getExchange())
                .flatMap(matchResult -> {
                    if (matchResult.isMatch()) {
                        var principal = authentication.getPrincipal();
                        if (principal instanceof CredentialsContainer credentialsContainer) {
                            credentialsContainer.eraseCredentials();
                        }
                        return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(principal)
                            .flatMap(serverResponse ->
                                serverResponse.writeTo(webFilterExchange.getExchange(), context));
                    }
                    return defaultHandler.onAuthenticationSuccess(webFilterExchange,
                        authentication);
                });
        }
    }

    public static class FormLoginFailureHandler implements ServerAuthenticationFailureHandler {

        private final ServerAuthenticationFailureHandler defaultHandler =
            new RedirectServerAuthenticationFailureHandler("/console?error#/login");
        private final ServerResponse.Context context;

        public FormLoginFailureHandler(ServerResponse.Context context) {
            this.context = context;
        }

        @Override
        public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange,
            AuthenticationException exception) {
            return ignoringMediaTypeAll(MediaType.APPLICATION_JSON).matches(
                    webFilterExchange.getExchange())
                .flatMap(matchResult -> {
                    if (matchResult.isMatch()) {
                        return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "error", exception.getLocalizedMessage()
                            ))
                            .flatMap(serverResponse -> serverResponse.writeTo(
                                webFilterExchange.getExchange(), context));
                    }
                    return defaultHandler.onAuthenticationFailure(webFilterExchange, exception);
                });
        }

    }
}
