package run.halo.app.plugin;

import java.util.Collection;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

public interface PluginRouterFunctionRegistry {
    void register(Collection<RouterFunction<ServerResponse>> routerFunctions);

    void unregister(Collection<RouterFunction<ServerResponse>> routerFunctions);

}
