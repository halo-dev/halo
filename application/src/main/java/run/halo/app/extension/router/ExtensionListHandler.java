package run.halo.app.extension.router;

import static run.halo.app.extension.router.ExtensionRouterFunctionFactory.PathPatternGenerator.buildExtensionPathPattern;

import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.router.ExtensionRouterFunctionFactory.ListHandler;

class ExtensionListHandler implements ListHandler {
    private final Scheme scheme;

    private final ReactiveExtensionClient client;

    public ExtensionListHandler(Scheme scheme, ReactiveExtensionClient client) {
        this.scheme = scheme;
        this.client = client;
    }

    @Override
    @NonNull
    public Mono<ServerResponse> handle(@NonNull ServerRequest request) {
        var queryParams = new SortableRequest(request.exchange());
        return client.list(scheme.type(),
                queryParams.toPredicate(),
                queryParams.toComparator(),
                queryParams.getPage(),
                queryParams.getSize())
            .flatMap(listResult -> ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(listResult));
    }

    @Override
    public String pathPattern() {
        return buildExtensionPathPattern(scheme);
    }
}
