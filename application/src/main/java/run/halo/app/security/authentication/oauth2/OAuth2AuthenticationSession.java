package run.halo.app.security.authentication.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.security.LoginHandlerEnhancer;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSession {

    private final ReactiveUserDetailsService userDetailsService;

    private final ServerSecurityContextRepository securityContextRepository;

    private final LoginHandlerEnhancer loginHandlerEnhancer;

    public Mono<Void> establish(ServerWebExchange exchange, String username, OAuth2AuthenticationToken original) {
        return userDetailsService
                .findByUsername(username)
                .map(userDetails -> HaloOAuth2AuthenticationToken.authenticated(userDetails, original))
                .flatMap(authentication -> securityContextRepository
                        .save(exchange, new SecurityContextImpl(authentication))
                        .then(loginHandlerEnhancer.onLoginSuccess(exchange, authentication)));
    }
}
