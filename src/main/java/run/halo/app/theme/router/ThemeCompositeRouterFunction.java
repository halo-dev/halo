package run.halo.app.theme.router;

import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.theme.router.strategy.TemplateRouterManager;

/**
 * <p>Theme template composite {@link RouterFunction} for manage routers for default templates.</p>
 * It routes specific requests to the {@link RouterFunction} maintained by the
 * {@link TemplateRouterManager}.
 *
 * @author guqing
 * @see TemplateRouterManager
 * @since 2.0.0
 */
@Component
public class ThemeCompositeRouterFunction implements
    RouterFunction<ServerResponse> {

    private final TemplateRouterManager templateRouterManager;

    public ThemeCompositeRouterFunction(TemplateRouterManager templateRouterManager) {
        this.templateRouterManager = templateRouterManager;
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
        return List.copyOf(templateRouterManager.getRouterFunctionMap().values());
    }
}
