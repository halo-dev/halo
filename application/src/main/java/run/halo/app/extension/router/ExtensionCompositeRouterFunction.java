package run.halo.app.extension.router;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.event.SchemeAddedEvent;
import run.halo.app.extension.event.SchemeRemovedEvent;

@Component
public class ExtensionCompositeRouterFunction implements RouterFunction<ServerResponse> {

    private final ConcurrentMap<Scheme, RouterFunction<ServerResponse>> schemeRouterFuncMapper;

    private final ReactiveExtensionClient client;

    public ExtensionCompositeRouterFunction(ReactiveExtensionClient client) {
        this.client = client;
        schemeRouterFuncMapper = new ConcurrentHashMap<>();
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

    private Iterable<RouterFunction<ServerResponse>> getRouterFunctions() {
        // TODO Copy router functions here
        return Collections.unmodifiableCollection(schemeRouterFuncMapper.values());
    }

    @EventListener
    void onSchemeAddedEvent(SchemeAddedEvent event) {
        var scheme = event.getScheme();
        var factory = new ExtensionRouterFunctionFactory(scheme, client);
        this.schemeRouterFuncMapper.put(scheme, factory.create());
    }

    @EventListener
    void onSchemeRemovedEvent(SchemeRemovedEvent event) {
        this.schemeRouterFuncMapper.remove(event.getScheme());
    }

}
