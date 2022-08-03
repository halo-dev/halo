package run.halo.app.extension.router;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.exception.ExtensionNotFoundException;

class ExtensionDeleteHandler implements ExtensionRouterFunctionFactory.DeleteHandler {

    private final Scheme scheme;

    private final ExtensionClient client;

    ExtensionDeleteHandler(Scheme scheme, ExtensionClient client) {
        this.scheme = scheme;
        this.client = client;
    }

    @Override
    public Mono<ServerResponse> handle(ServerRequest request) {
        String name = request.pathVariable("name");
        return getExtension(name)
            .flatMap(extension ->
                Mono.fromRunnable(() -> client.delete(extension)).thenReturn(extension))
            .flatMap(extension -> this.getExtension(name))
            .flatMap(extension -> ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(extension));
    }

    private Mono<? extends Extension> getExtension(String name) {
        return Mono.justOrEmpty(client.fetch(scheme.type(), name))
            .switchIfEmpty(Mono.error(() -> new ExtensionNotFoundException(
                "Extension with name " + name + " was not found")));
    }

    @Override
    public String pathPattern() {
        return ExtensionRouterFunctionFactory.PathPatternGenerator.buildExtensionPathPattern(scheme)
            + "/{name}";
    }

}
