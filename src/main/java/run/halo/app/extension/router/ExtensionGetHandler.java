package run.halo.app.extension.router;

import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.exception.ExtensionNotFoundException;

class ExtensionGetHandler implements ExtensionRouterFunctionFactory.GetHandler {
    private final Scheme scheme;

    private final ExtensionClient client;

    public ExtensionGetHandler(Scheme scheme, ExtensionClient client) {
        this.scheme = scheme;
        this.client = client;
    }

    @Override
    public String pathPattern() {
        return ExtensionRouterFunctionFactory.PathPatternGenerator.buildExtensionPathPattern(scheme)
            + "/{name}";
    }

    @Override
    @NonNull
    public Mono<ServerResponse> handle(@NonNull ServerRequest request) {
        var extensionName = request.pathVariable("name");

        var extension = client.fetch(scheme.type(), extensionName)
            .orElseThrow(() -> new ExtensionNotFoundException(
                scheme.groupVersionKind() + " was not found"));
        return ServerResponse
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(extension);
    }
}
