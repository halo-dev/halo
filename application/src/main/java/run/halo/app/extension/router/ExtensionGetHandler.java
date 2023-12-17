package run.halo.app.extension.router;

import static run.halo.app.extension.router.ExtensionRouterFunctionFactory.PathPatternGenerator.buildExtensionPathPattern;

import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.router.ExtensionRouterFunctionFactory.GetHandler;

class ExtensionGetHandler implements GetHandler {
    private final Scheme scheme;

    private final ReactiveExtensionClient client;

    public ExtensionGetHandler(Scheme scheme, ReactiveExtensionClient client) {
        this.scheme = scheme;
        this.client = client;
    }

    @Override
    public String pathPattern() {
        return buildExtensionPathPattern(scheme) + "/{name}";
    }

    @Override
    @NonNull
    public Mono<ServerResponse> handle(@NonNull ServerRequest request) {
        var extensionName = request.pathVariable("name");

        return client.get(scheme.type(), extensionName)
            .flatMap(extension -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(extension));
    }
}
