package run.halo.app.theme;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * @author guqing
 * @since 2.0.0
 */
public class ThemeCompositeRouterFunction implements
    RouterFunction<ServerResponse> {
    private final Map<String, RouterFunction<ServerResponse>> themeRouterFuncMapper;

    private final ReactiveExtensionClient client;

    public ThemeCompositeRouterFunction(ReactiveExtensionClient client) {
        this.client = client;
        themeRouterFuncMapper = new ConcurrentHashMap<>();
    }

    @Override
    @NonNull
    public Mono<HandlerFunction<ServerResponse>> route(@NonNull ServerRequest request) {
        return Flux.fromIterable(getRouterFunctions())
            .concatMap(routerFunction -> routerFunction.route(request))
            .next();
    }

    @Override
    public void accept(@NonNull RouterFunctions.Visitor visitor) {
        getRouterFunctions().forEach(routerFunction -> routerFunction.accept(visitor));
    }

    private List<RouterFunction<ServerResponse>> getRouterFunctions() {
        return List.copyOf(themeRouterFuncMapper.values());
    }
}
