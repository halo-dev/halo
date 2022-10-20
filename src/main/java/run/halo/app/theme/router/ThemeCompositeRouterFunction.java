package run.halo.app.theme.router;

import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * <p>Theme template composite {@link RouterFunction} for manage routers for default templates.</p>
 * It routes specific requests to the {@link RouterFunction} maintained by the
 * {@link PermalinkHttpGetRouter}.
 *
 * @author guqing
 * @see PermalinkHttpGetRouter
 * @since 2.0.0
 */
@Component
public class ThemeCompositeRouterFunction implements
    RouterFunction<ServerResponse> {

    private final PermalinkHttpGetRouter permalinkHttpGetRouter;

    public ThemeCompositeRouterFunction(PermalinkHttpGetRouter permalinkHttpGetRouter) {
        this.permalinkHttpGetRouter = permalinkHttpGetRouter;
    }

    @Override
    @NonNull
    public Mono<HandlerFunction<ServerResponse>> route(@NonNull ServerRequest request) {
        // this router function only supports GET method
        if (!request.method().equals(HttpMethod.GET)) {
            return Mono.empty();
        }
        return Mono.justOrEmpty(permalinkHttpGetRouter.route(request));
    }
}
