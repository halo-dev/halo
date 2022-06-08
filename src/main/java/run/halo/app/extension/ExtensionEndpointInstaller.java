package run.halo.app.extension;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.extension.exception.ExtensionConvertException;
import run.halo.app.extension.exception.ExtensionNotFoundException;

public class ExtensionEndpointInstaller {

    public static List<RouterFunction<ServerResponse>> install(Scheme scheme,
        ExtensionClient client) {

        var routers = new ArrayList<RouterFunction<ServerResponse>>(6);
        var getHandler = new ExtensionGetHandler(scheme, client);
        var listHandler = new ExtensionListHandler(scheme, client);
        var createHandler = new ExtensionCreateHandler(scheme, client);
        // TODO More handlers here
        routers.add(route()
            .GET(getHandler.pathPattern(), getHandler)
            .GET(listHandler.pathPattern(), listHandler)
            .POST(createHandler.pathPattern(), createHandler)
            .build());

        return routers;
    }

    public interface PathPatternGenerator {

        String pathPattern();

    }

    public interface GetHandler extends HandlerFunction<ServerResponse>, PathPatternGenerator {

    }

    public interface ListHandler extends HandlerFunction<ServerResponse>, PathPatternGenerator {

    }

    public interface CreateHandler extends HandlerFunction<ServerResponse>, PathPatternGenerator {

    }

    public static class ExtensionCreateHandler implements CreateHandler {

        private final Scheme scheme;

        private final ExtensionClient client;

        public ExtensionCreateHandler(Scheme scheme, ExtensionClient client) {
            this.scheme = scheme;
            this.client = client;
        }

        @Override
        public Mono<ServerResponse> handle(ServerRequest request) {
            return request.bodyToMono(Unstructured.class)
                .switchIfEmpty(Mono.error(() -> new ExtensionConvertException(
                    "Cannot read body to " + scheme.groupVersionKind())))
                .doOnSuccess(client::create)
                .map(unstructured ->
                    client.fetch(scheme.type(), unstructured.getMetadata().getName())
                        .orElseThrow(() -> new ExtensionNotFoundException(
                            scheme.groupVersionKind() + " " + unstructured.getMetadata().getName()
                                + "was not found")))
                .map(extension -> ServerResponse
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(extension))
                .cast(ServerResponse.class);
        }

        @Override
        public String pathPattern() {
            var gvk = scheme.groupVersionKind();
            return "/apis/" + gvk.group() + "/" + gvk.version() + "/" + scheme.plural();
        }
    }

    public static class ExtensionListHandler implements ListHandler {

        private final Scheme scheme;

        private final ExtensionClient client;

        public ExtensionListHandler(Scheme scheme, ExtensionClient client) {
            this.scheme = scheme;
            this.client = client;
        }

        @Override
        public Mono<ServerResponse> handle(ServerRequest request) {
            // TODO Resolve predicate and comparator
            var extensions = client.list(scheme.type(), null, null);
            return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(extensions);
        }

        @Override
        public String pathPattern() {
            var gvk = scheme.groupVersionKind();
            return "/apis/" + gvk.group() + "/" + gvk.version() + "/" + scheme.plural();
        }
    }

    public static class ExtensionGetHandler implements GetHandler {
        private final Scheme scheme;

        private final ExtensionClient client;

        public ExtensionGetHandler(Scheme scheme, ExtensionClient client) {
            this.scheme = scheme;
            this.client = client;
        }

        @Override
        public String pathPattern() {
            var gvk = scheme.groupVersionKind();
            return "/apis/" + gvk.group() + "/" + gvk.version() + "/" + scheme.plural() + "/{name}";
        }

        @Override
        @NonNull
        public Mono<ServerResponse> handle(ServerRequest request) {
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
