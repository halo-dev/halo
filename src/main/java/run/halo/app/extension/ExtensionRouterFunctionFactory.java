package run.halo.app.extension;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
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
        // TODO More handlers here
        return route()
            .GET(getHandler.pathPattern(), getHandler)
            .GET(listHandler.pathPattern(), listHandler)
            .POST(createHandler.pathPattern(), createHandler)
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
                .doOnSuccess(client::create)
                .map(unstructured ->
                    client.fetch(scheme.type(), unstructured.getMetadata().getName())
                        .orElseThrow(() -> new ExtensionNotFoundException(
                            scheme.groupVersionKind() + " " + unstructured.getMetadata().getName()
                                + "was not found")))
                .flatMap(extension -> ServerResponse
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(extension))
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

}
