package run.halo.app.extension.router;

import static run.halo.app.extension.router.ExtensionRouterFunctionFactory.PathPatternGenerator.buildExtensionPathPattern;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import reactor.core.publisher.Mono;
import run.halo.app.extension.JsonExtension;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.router.ExtensionRouterFunctionFactory.PatchHandler;

/**
 * Handler for patching extension.
 *
 * @author johnniang
 */
public class ExtensionPatchHandler implements PatchHandler {

    private static final MediaType JSON_PATCH_MEDIA_TYPE =
        MediaType.valueOf("application/json-patch+json");

    private final Scheme scheme;

    private final ReactiveExtensionClient client;

    public ExtensionPatchHandler(Scheme scheme, ReactiveExtensionClient client) {
        this.scheme = scheme;
        this.client = client;
    }

    @Override
    public Mono<ServerResponse> handle(ServerRequest request) {
        var name = request.pathVariable("name");
        var contentTypeOpt = request.headers().contentType();
        if (contentTypeOpt.isEmpty()) {
            return Mono.error(
                new UnsupportedMediaTypeStatusException((MediaType) null,
                    List.of(JSON_PATCH_MEDIA_TYPE))
            );
        }
        var contentType = contentTypeOpt.get();
        if (!contentType.isCompatibleWith(JSON_PATCH_MEDIA_TYPE)) {
            return Mono.error(
                new UnsupportedMediaTypeStatusException(contentType, List.of(JSON_PATCH_MEDIA_TYPE))
            );
        }

        return request.bodyToMono(JsonPatch.class)
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException("Request body required.")))
            .flatMap(jsonPatch -> client.getJsonExtension(scheme.groupVersionKind(), name)
                .flatMap(jsonExtension -> {
                    try {
                        // apply the patch
                        var appliedJsonNode =
                            (ObjectNode) jsonPatch.apply(jsonExtension.getInternal());
                        var patchedExtension =
                            new JsonExtension(jsonExtension.getObjectMapper(), appliedJsonNode);
                        // update the patched extension
                        return client.update(patchedExtension);
                    } catch (JsonPatchException e) {
                        return Mono.error(e);
                    }
                }))
            .flatMap(updated -> ServerResponse.ok().bodyValue(updated));
    }

    @Override
    public String pathPattern() {
        return buildExtensionPathPattern(scheme) + "/{name}";
    }

}
