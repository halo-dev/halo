package run.halo.app.security.sudo;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.infra.exception.AccessDeniedException;

/**
 * Enforces sudo confirmation on sensitive APIs and rejects token auth on sudo endpoints.
 *
 * @author johnniang
 * @since 2.27.0
 */
class SudoModeWebFilter implements WebFilter {

    static final String SUDO_API_PATH = "/apis/uc.api.security.halo.run/v1alpha1/authentications/sudo";

    static final String SUDO_API_PATH_NESTED = "/apis/uc.api.security.halo.run/v1alpha1/authentications/sudo/**";

    private final SudoService sudoService;
    private final ServerWebExchangeMatcher sudoApiMatcher;
    private final ServerWebExchangeMatcher sensitiveMatcher;

    SudoModeWebFilter(SudoService sudoService) {
        this.sudoService = sudoService;
        this.sudoApiMatcher =
                new OrServerWebExchangeMatcher(pathMatchers(SUDO_API_PATH), pathMatchers(SUDO_API_PATH_NESTED));
        this.sensitiveMatcher = createSensitiveMatcher();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return sudoApiMatcher.matches(exchange).flatMap(sudoApi -> {
            if (sudoApi.isMatch()) {
                return rejectTokenAuthentication().then(Mono.defer(() -> chain.filter(exchange)));
            }
            return sensitiveMatcher.matches(exchange).flatMap(sensitive -> {
                if (!sensitive.isMatch()) {
                    return chain.filter(exchange);
                }
                return enforceSudo(exchange).then(Mono.defer(() -> chain.filter(exchange)));
            });
        });
    }

    private Mono<Void> rejectTokenAuthentication() {
        return ReactiveSecurityContextHolder.getContext()
                .mapNotNull(SecurityContext::getAuthentication)
                .filter(SudoService::isJwtAuthentication)
                .flatMap(auth -> Mono.error(new AccessDeniedException("Sudo APIs require a browser session")));
    }

    private Mono<Void> enforceSudo(ServerWebExchange exchange) {
        return ReactiveSecurityContextHolder.getContext()
                .mapNotNull(SecurityContext::getAuthentication)
                .filter(SudoService::isSessionAuthentication)
                .flatMap(auth -> sudoService.requireSudo(exchange, auth.getName()));
    }

    private static ServerWebExchangeMatcher createSensitiveMatcher() {
        return new OrServerWebExchangeMatcher(
                pathMatchers(
                        HttpMethod.PUT,
                        "/apis/uc.api.halo.run/v1alpha1/users/-/password",
                        "/apis/api.console.halo.run/v1alpha1/users/-/password",
                        "/apis/api.console.halo.run/v1alpha1/users/*/password"),
                pathMatchers(HttpMethod.POST, "/apis/uc.api.security.halo.run/v1alpha1/personalaccesstokens"),
                pathMatchers(HttpMethod.DELETE, "/apis/uc.api.security.halo.run/v1alpha1/personalaccesstokens/*"),
                pathMatchers(
                        HttpMethod.PUT,
                        "/apis/uc.api.security.halo.run/v1alpha1/personalaccesstokens/*/actions/revocation",
                        "/apis/uc.api.security.halo.run/v1alpha1/personalaccesstokens/*/actions/restoration"));
    }
}
