package run.halo.app.theme.endpoint;

import org.springframework.web.server.ServerWebExchange;
import run.halo.app.extension.router.SortableRequest;

/**
 * Query parameters for post public APIs.
 *
 * @author guqing
 * @since 2.5.0
 */
public class PostPublicQuery extends SortableRequest {

    public PostPublicQuery(ServerWebExchange exchange) {
        super(exchange);
    }
}