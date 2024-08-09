package run.halo.app.core.attachment.endpoint;

import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.CacheControl;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.LocalThumbnailService;
import run.halo.app.core.attachment.ThumbnailSize;

/**
 * Local thumbnail endpoint for local thumbnail resource access.
 *
 * @author guqing
 * @since 2.19.0
 */
@Component
@RequiredArgsConstructor
public class LocalThumbnailEndpoint {
    private final LocalThumbnailService localThumbnailService;
    private final WebProperties webProperties;

    @Bean
    RouterFunction<ServerResponse> localThumbnailRouter() {
        var resourceProperties = webProperties.getResources();
        final var useLastModified = resourceProperties.getCache().isUseLastModified();
        final var cacheControl = getCacheControl(resourceProperties);
        return RouterFunctions.route()
            .GET("/upload/thumbnails/{year}/w{width}/{fileName}", request -> {
                var width = request.pathVariable("width");
                var year = request.pathVariable("year");
                var fileName = request.pathVariable("fileName");
                var size = ThumbnailSize.fromWidth(width);
                return localThumbnailService.getThumbnail(year, size, fileName)
                    .flatMap(resource -> {
                        var bodyBuilder = ServerResponse.ok()
                            .cacheControl(cacheControl);
                        try {
                            if (useLastModified) {
                                var lastModified = Instant.ofEpochMilli(resource.lastModified());
                                return request.checkNotModified(lastModified)
                                    .switchIfEmpty(
                                        Mono.defer(() -> bodyBuilder.lastModified(lastModified)
                                            .body(BodyInserters.fromResource(resource))));
                            }
                            return bodyBuilder.body(BodyInserters.fromResource(resource));
                        } catch (IOException e) {
                            return Mono.error(e);
                        }
                    });
            })
            .build();
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
