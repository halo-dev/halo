package run.halo.app.extension.router;

import static run.halo.app.extension.router.ExtensionRouterFunctionFactory.PathPatternGenerator.buildExtensionPathPattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.router.ExtensionRouterFunctionFactory.PatchHandler;
import run.halo.app.infra.utils.JsonUtils;

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

        return request.bodyToMono(new ParameterizedTypeReference<List<Map<?, ?>>>() {
            })
            // we have to use the old mapper to convert the map to JsonPatch
            .map(maps -> JsonUtils.mapper().convertValue(maps, JsonPatch.class))
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException("Request body required.")))
            .flatMap(jsonPatch -> client.get(scheme.type(), name)
                .flatMap(extension -> {
                    var mapper = JsonUtils.mapper();
                    var jsonNode = mapper.convertValue(extension, JsonNode.class);
                    JsonNode patchedNode;
                    try {
                        patchedNode = jsonPatch.apply(jsonNode);
                    } catch (JsonPatchException e) {
                        return Mono.error(e);
                    }
                    var patchedExtension =
                        mapper.convertValue(patchedNode, extension.getClass());
                    return client.update(patchedExtension);
                })
            )
            .flatMap(updated -> ServerResponse.ok().bodyValue(updated));
    }

    @Override
    public String pathPattern() {
        return buildExtensionPathPattern(scheme) + "/{name}";
    }

}
