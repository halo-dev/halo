package run.halo.app.extension;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.extension.exception.ExtensionConvertException;
import run.halo.app.extension.exception.ExtensionNotFoundException;

public class ExtensionRouterFunctionFactory {

    private final Scheme scheme;

    private final ExtensionClient client;

    public ExtensionRouterFunctionFactory(Scheme scheme, ExtensionClient client) {
        this.scheme = scheme;
        this.client = client;
    }

    @NonNull
    public RouterFunction<ServerResponse> create() {
        var getHandler = new ExtensionGetHandler(scheme, client);
        var listHandler = new ExtensionListHandler(scheme, client);
        var createHandler = new ExtensionCreateHandler(scheme, client);
        var updateHandler = new ExtensionUpdateHandler(scheme, client);
        var deleteHandler = new ExtensionDeleteHandler(scheme, client);
        // TODO More handlers here
        return route()
            .GET(getHandler.pathPattern(), getHandler)
            .GET(listHandler.pathPattern(), listHandler)
            .POST(createHandler.pathPattern(), createHandler)
            .PUT(updateHandler.pathPattern(), updateHandler)
            .DELETE(deleteHandler.pathPattern(), deleteHandler)
            .build();
    }

    interface PathPatternGenerator {

        String pathPattern();

        static String buildExtensionPathPattern(Scheme scheme) {
            var gvk = scheme.groupVersionKind();
            StringBuilder pattern = new StringBuilder();
            if (gvk.hasGroup()) {
                pattern.append("/apis/").append(gvk.group());
            } else {
                pattern.append("/api");
            }
            return pattern.append('/').append(gvk.version()).append('/').append(scheme.plural())
                .toString();
        }
    }

    interface GetHandler extends HandlerFunction<ServerResponse>, PathPatternGenerator {

    }

    interface ListHandler extends HandlerFunction<ServerResponse>, PathPatternGenerator {

    }

    interface CreateHandler extends HandlerFunction<ServerResponse>, PathPatternGenerator {

    }

    interface UpdateHandler extends HandlerFunction<ServerResponse>, PathPatternGenerator {

    }

    interface DeleteHandler extends HandlerFunction<ServerResponse>, PathPatternGenerator {

    }

    static class ExtensionCreateHandler implements CreateHandler {

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
            return PathPatternGenerator.buildExtensionPathPattern(scheme);
        }
    }

    static class ExtensionListHandler implements ListHandler {

        private final Scheme scheme;

        private final ExtensionClient client;

        public ExtensionListHandler(Scheme scheme, ExtensionClient client) {
            this.scheme = scheme;
            this.client = client;
        }

        @Override
        @NonNull
        public Mono<ServerResponse> handle(@NonNull ServerRequest request) {
            // TODO Resolve predicate and comparator from request
            var extensions = client.list(scheme.type(), null, null);
            return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(extensions);
        }

        @Override
        public String pathPattern() {
            return PathPatternGenerator.buildExtensionPathPattern(scheme);
        }
    }

    static class ExtensionGetHandler implements GetHandler {
        private final Scheme scheme;

        private final ExtensionClient client;

        public ExtensionGetHandler(Scheme scheme, ExtensionClient client) {
            this.scheme = scheme;
            this.client = client;
        }

        @Override
        public String pathPattern() {
            return PathPatternGenerator.buildExtensionPathPattern(scheme) + "/{name}";
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

    static class ExtensionUpdateHandler implements UpdateHandler {

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
            return PathPatternGenerator.buildExtensionPathPattern(scheme) + "/{name}";
        }
    }

    static class ExtensionDeleteHandler implements DeleteHandler {

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
                    Mono.fromRunnable(() -> {
                        extension.getMetadata().setDeletionTimestamp(Instant.now());
                        client.update(extension);
                    }).thenReturn(extension))
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
            return PathPatternGenerator.buildExtensionPathPattern(scheme) + "/{name}";
        }

    }
}
