package run.halo.app.core.endpoint.theme;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.net.URI;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.schema.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.attachment.thumbnail.ThumbnailService;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

/**
 * Thumbnail endpoint for thumbnail resource access.
 *
 * @author guqing
 * @author johnniang
 * @since 2.19.0
 */
@Component
@RequiredArgsConstructor
public class ThumbnailEndpoint implements CustomEndpoint {

    private final ThumbnailService thumbnailService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "ThumbnailV1alpha1Public";
        return SpringdocRouteBuilder.route()
            .GET("/thumbnails/-/via-uri", this::getThumbnailByUri, builder -> {
                builder.operationId("GetThumbnailByUri")
                    .description("Get thumbnail by URI")
                    .tag(tag)
                    .response(responseBuilder().implementation(Resource.class))
                    .parameter(parameterBuilder()
                        .in(ParameterIn.QUERY)
                        .name("uri")
                        .description("The URI of the image")
                        .required(true)
                    )
                    .parameter(parameterBuilder()
                        .in(ParameterIn.QUERY)
                        .name("size")

                        .implementation(ThumbnailSize.class)
                        .description("The size of the thumbnail")
                        .required(true)
                    )
                    .parameter(parameterBuilder()
                        .in(ParameterIn.QUERY)
                        .name("width")
                        .schema(Builder.schemaBuilder()
                            .type("integer")
                            .allowableValues(Arrays.stream(ThumbnailSize.allowedWidths())
                                .map(String::valueOf)
                                .toArray(String[]::new)
                            )
                        )
                        .description("""
                            The width of the thumbnail, if 'size' is not provided, this \
                            parameter will be used to determine the size\
                            """)
                        .required(false)
                    );
            })
            .build();
    }

    private Mono<ServerResponse> getThumbnailByUri(ServerRequest request) {
        var uri = request.queryParam("uri")
            .filter(StringUtils::isNotBlank)
            .map(URI::create);
        if (uri.isEmpty()) {
            return Mono.error(
                new ServerWebInputException("Required parameter 'uri' is missing or invalid")
            );
        }
        var size = request.queryParam("size")
            .filter(StringUtils::isNotBlank)
            .flatMap(ThumbnailSize::optionalValueOf)
            .or(() -> request.queryParam("width")
                .filter(StringUtils::isNotBlank)
                .map(ThumbnailSize::fromWidth)
            );
        if (size.isEmpty()) {
            return Mono.error(new ServerWebInputException(
                "Required parameter 'size' or 'width' is missing or invalid"
            ));
        }
        return thumbnailService.get(uri.get(), size.get())
            .defaultIfEmpty(uri.get())
            .flatMap(thumbnailLink -> ServerResponse.status(HttpStatus.FOUND)
                .location(thumbnailLink)
                .build()
            );
    }

    @Override
    public GroupVersion groupVersion() {
        return new GroupVersion("api.storage.halo.run", "v1alpha1");
    }

}
