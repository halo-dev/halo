package run.halo.app.config;

import static org.springframework.security.core.context.ReactiveSecurityContextHolder.withSecurityContext;

import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.authentication.AnonymousAuthenticationWebFilter;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * HaloAnonymousAuthenticationWebFilter will save SecurityContext into SecurityContextRepository
 * when AnonymousAuthenticationToken is created.
 *
 * @author johnniang
 */
public class HaloAnonymousAuthenticationWebFilter extends AnonymousAuthenticationWebFilter {

    private final ServerSecurityContextRepository securityContextRepository;

    public HaloAnonymousAuthenticationWebFilter(String key, Object principal,
        List<GrantedAuthority> authorities,
        ServerSecurityContextRepository securityContextRepository) {
        super(key, principal, authorities);
        this.securityContextRepository = securityContextRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext().switchIfEmpty(Mono.defer(() -> {
            var authentication = createAuthentication(exchange);
            var securityContext = new SecurityContextImpl(authentication);
            return securityContextRepository.save(exchange, securityContext)
                .then(Mono.defer(() -> chain.filter(exchange)
                    .contextWrite(withSecurityContext(Mono.just(securityContext)))
                    .then(Mono.empty())));
        })).flatMap(securityContext -> chain.filter(exchange));
    }
}
