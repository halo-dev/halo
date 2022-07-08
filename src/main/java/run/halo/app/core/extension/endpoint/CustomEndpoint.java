package run.halo.app.core.extension.endpoint;

import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * RouterFunction provider for custom endpoints.
 *
 * @author johnniang
 */
@FunctionalInterface
public interface CustomEndpoint {

    RouterFunction<ServerResponse> endpoint();

}
