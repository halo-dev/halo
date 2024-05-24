package run.halo.app.security.authentication.rememberme;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RememberMeAuthenticationFilter implements WebFilter {
    private final ServerSecurityContextRepository securityContextRepository;
    private final RememberMeServices rememberMeServices;
    private final RememberMeAuthenticationManager rememberMeAuthenticationManager;

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return securityContextRepository.load(exchange)
            .switchIfEmpty(Mono.defer(() -> rememberMeServices.autoLogin(exchange)
                .flatMap(rememberMeAuthenticationManager::authenticate)
                .flatMap(authentication -> {
                    var securityContext = new SecurityContextImpl(authentication);
                    return securityContextRepository.save(exchange, securityContext);
                })
                .onErrorResume(AuthenticationException.class,
                    e -> rememberMeServices.loginFail(exchange)
                )
                .then(Mono.empty())
            ))
            .then(chain.filter(exchange));
    }
}
