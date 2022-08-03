package run.halo.app.extension.router;

import java.net.URI;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.Unstructured;
import run.halo.app.extension.exception.ExtensionConvertException;
import run.halo.app.extension.exception.ExtensionNotFoundException;

class ExtensionCreateHandler implements ExtensionRouterFunctionFactory.CreateHandler {

    private final Scheme scheme;

    private final ExtensionClient client;

    public ExtensionCreateHandler(Scheme scheme, ExtensionClient client) {
        this.scheme = scheme;
        this.client = client;
    }

    @Override
    @NonNull
    public Mono<ServerResponse> handle(@NonNull ServerRequest request) {
        return request.bodyToMono(Unstructured.class)
            .switchIfEmpty(Mono.error(() -> new ExtensionConvertException(
                "Cannot read body to " + scheme.groupVersionKind())))
            .flatMap(extToCreate -> Mono.fromCallable(() -> {
                var name = extToCreate.getMetadata().getName();
                client.create(extToCreate);
                return client.fetch(scheme.type(), name)
                    .orElseThrow(() -> new ExtensionNotFoundException(
                        "Extension with name " + name + " was not found"));
            }))
            .flatMap(createdExt -> ServerResponse
                .created(URI.create(pathPattern() + "/" + createdExt.getMetadata().getName()))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createdExt))
            .cast(ServerResponse.class);
    }

    @Override
    public String pathPattern() {
        return ExtensionRouterFunctionFactory.PathPatternGenerator.buildExtensionPathPattern(
            scheme);
    }
}
