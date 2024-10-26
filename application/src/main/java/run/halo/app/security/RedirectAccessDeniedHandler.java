package run.halo.app.security;

import java.net.URI;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Redirect access denied handler.
 *
 * @author johnniang
 * @since 2.20.0
 */
public class RedirectAccessDeniedHandler implements ServerAccessDeniedHandler {

    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    private final URI redirectUri;

    public RedirectAccessDeniedHandler(String redirectUri) {
        this.redirectUri = URI.create(redirectUri);
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        return redirectStrategy.sendRedirect(exchange, redirectUri);
    }
}
