package run.halo.app.extension.router;

import java.util.Objects;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.Unstructured;
import run.halo.app.extension.exception.ExtensionConvertException;
import run.halo.app.extension.exception.ExtensionNotFoundException;

class ExtensionUpdateHandler implements ExtensionRouterFunctionFactory.UpdateHandler {

    private final Scheme scheme;

    private final ExtensionClient client;

    ExtensionUpdateHandler(Scheme scheme, ExtensionClient client) {
        this.scheme = scheme;
        this.client = client;
    }

    @Override
    public Mono<ServerResponse> handle(ServerRequest request) {
        String name = request.pathVariable("name");
        return request.bodyToMono(Unstructured.class)
            .filter(unstructured -> unstructured.getMetadata() != null
                && StringUtils.hasText(unstructured.getMetadata().getName())
                && Objects.equals(unstructured.getMetadata().getName(), name))
            .switchIfEmpty(Mono.error(() -> new ExtensionConvertException(
                "Cannot read body to " + scheme.groupVersionKind())))
            .flatMap(extToUpdate -> Mono.fromCallable(() -> {
                client.update(extToUpdate);
                return client.fetch(scheme.type(), name)
                    .orElseThrow(() -> new ExtensionNotFoundException(
                        "Extension with name " + name + " was not found"));
            }))
            .flatMap(updated -> ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updated));
    }

    @Override
    public String pathPattern() {
        return ExtensionRouterFunctionFactory.PathPatternGenerator.buildExtensionPathPattern(scheme)
            + "/{name}";
    }
}
