package run.halo.app.core.endpoint.theme;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.LocalThumbnailService;
import run.halo.app.core.attachment.ThumbnailService;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

/**
 * Thumbnail endpoint for thumbnail resource access.
 *
 * @author guqing
 * @since 2.19.0
 */
@Component
@RequiredArgsConstructor
public class ThumbnailEndpoint implements CustomEndpoint {
    private final WebClient webClient = WebClient.builder().build();
    private final LocalThumbnailService localThumbnailService;
    private final WebProperties webProperties;
    private final ThumbnailService thumbnailService;

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
        return thumbnailService.get(query.getUri(), query.getSize())
            .filterWhen(uri -> isAccessible(request, uri))
            .defaultIfEmpty(query.getUri())
            .flatMap(uri -> ServerResponse.temporaryRedirect(uri).build());
    }

    Mono<Boolean> isAccessible(ServerRequest request, URI uri) {
        var url = Optional.of(uri)
            .filter(URI::isAbsolute)
            .orElseGet(() -> request.uriBuilder().replacePath(uri.toASCIIString()).build());
        // resource handler does not support head access for Halo, so use get request here
        return webClient.get()
            .uri(url)
            // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Range
            .header(HttpHeaders.RANGE, "bytes=0-0")
            .exchangeToMono(response -> {
                var statusCode = response.statusCode();
                return Mono.just(statusCode.is2xxSuccessful() || statusCode.is3xxRedirection());
            })
            .onErrorReturn(false)
            .defaultIfEmpty(false);
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

    @Bean
    RouterFunction<ServerResponse> localThumbnailResourceRouter() {
        return RouterFunctions.route()
            .GET("/upload/thumbnails/{year}/w{width}/{fileName}", request -> {
                var width = request.pathVariable("width");
                var year = request.pathVariable("year");
                var fileName = request.pathVariable("fileName");
                var size = ThumbnailSize.fromWidth(width);
                var thumbnailUri = localThumbnailService.buildThumbnailUri(year, size, fileName);
                return localThumbnailService.getThumbnail(thumbnailUri)
                    .flatMap(resource -> getResourceResponse(request, resource))
                    .switchIfEmpty(Mono.defer(
                        () -> localThumbnailService.getOriginalImageUri(thumbnailUri)
                            .flatMap(this::fallback))
                    );
            })

            .build();
    }

    @Override
    public GroupVersion groupVersion() {
        return new GroupVersion("api.storage.halo.run", "v1alpha1");
    }

    private Mono<ServerResponse> getResourceResponse(ServerRequest request, Resource resource) {
        var resourceProperties = webProperties.getResources();
        final var useLastModified = resourceProperties.getCache().isUseLastModified();
        final var cacheControl = getCacheControl(resourceProperties);
        var bodyBuilder = ServerResponse.ok().cacheControl(cacheControl);
        try {
            if (useLastModified) {
                var lastModified = Instant.ofEpochMilli(resource.lastModified());
                return request.checkNotModified(lastModified)
                    .switchIfEmpty(Mono.defer(() -> bodyBuilder.lastModified(lastModified)
                        .body(BodyInserters.fromResource(resource))
                    ));
            }
            return bodyBuilder.body(BodyInserters.fromResource(resource));
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    private Mono<ServerResponse> fallback(URI imageUri) {
        return ServerResponse.temporaryRedirect(imageUri).build();
    }

    private static CacheControl getCacheControl(WebProperties.Resources resourceProperties) {
        var cacheControl = resourceProperties.getCache()
            .getCachecontrol()
            .toHttpCacheControl();
        if (cacheControl == null) {
            cacheControl = CacheControl.empty();
        }
        return cacheControl;
    }
}
