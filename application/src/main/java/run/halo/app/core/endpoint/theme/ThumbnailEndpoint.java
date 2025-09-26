package run.halo.app.core.endpoint.theme;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.QueryFactory;

/**
 * Thumbnail endpoint for thumbnail resource access.
 *
 * @author guqing
 * @since 2.19.0
 */
@Component
@RequiredArgsConstructor
public class ThumbnailEndpoint implements CustomEndpoint {

    private final ReactiveExtensionClient client;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "ThumbnailV1alpha1Public";
        return SpringdocRouteBuilder.route()
            .GET("/thumbnails/-/via-uri", this::getThumbnailByUri, builder -> {
                builder.operationId("GetThumbnailByUri")
                    .description("Get thumbnail by URI")
                    .tag(tag)
                    .response(responseBuilder()
                        .implementation(Resource.class));
                ThumbnailQuery.buildParameters(builder);
            })
            .build();
    }

    private Mono<ServerResponse> getThumbnailByUri(ServerRequest request) {
        var query = new ThumbnailQuery(request.queryParams());
        var size = query.getSize();
        var uri = query.getUri().toASCIIString();
        var listOptions = ListOptions.builder()
            .andQuery(ExtensionUtil.notDeleting())
            .andQuery(QueryFactory.equal("status.permalink", uri))
            .build();
        // query by permalink
        return client.listAll(Attachment.class, listOptions, ExtensionUtil.defaultSort())
            // find the first one
            .next()
            .mapNotNull(attachment -> {
                var thumbnails = attachment.getStatus().getThumbnails();
                return thumbnails.get(size.name());
            })
            .defaultIfEmpty(uri)
            .flatMap(thumbnailLink -> ServerResponse.status(HttpStatus.FOUND)
                .location(URI.create(thumbnailLink))
                .build()
            );
    }

    static class ThumbnailQuery {
        private final MultiValueMap<String, String> params;

        public ThumbnailQuery(MultiValueMap<String, String> params) {
            this.params = params;
        }

        @Schema(requiredMode = REQUIRED)
        public URI getUri() {
            var uriStr = params.getFirst("uri");
            if (StringUtils.isBlank(uriStr)) {
                throw new ServerWebInputException("Required parameter 'uri' is missing");
            }
            try {
                return URI.create(uriStr);
            } catch (IllegalArgumentException e) {
                throw new ServerWebInputException("Invalid URI: " + uriStr);
            }
        }

        @Schema(requiredMode = REQUIRED)
        public ThumbnailSize getSize() {
            var size = params.getFirst("size");
            if (StringUtils.isBlank(size)) {
                throw new ServerWebInputException("Required parameter 'size' is missing");
            }
            return ThumbnailSize.fromName(size);
        }

        public static void buildParameters(Builder builder) {
            builder.parameter(parameterBuilder()
                    .in(ParameterIn.QUERY)
                    .name("uri")
                    .description("The URI of the image")
                    .required(true))
                .parameter(parameterBuilder()
                    .in(ParameterIn.QUERY)
                    .name("size")
                    .description("The size of the thumbnail,available values are s,m,l,xl")
                    .required(true));
        }
    }

    @Override
    public GroupVersion groupVersion() {
        return new GroupVersion("api.storage.halo.run", "v1alpha1");
    }

}
