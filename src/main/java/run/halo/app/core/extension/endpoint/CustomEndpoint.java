package run.halo.app.core.extension.endpoint;

import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.extension.GroupVersion;

/**
 * RouterFunction provider for custom endpoints.
 *
 * @author johnniang
 */
public interface CustomEndpoint {

    RouterFunction<ServerResponse> endpoint();

    default GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("api.console.halo.run/v1alpha1");
    }

}
