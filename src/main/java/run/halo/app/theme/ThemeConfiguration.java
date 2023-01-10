package run.halo.app.theme;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.FilePathUtils;
import run.halo.app.theme.dialect.LinkExpressionObjectDialect;

/**
 * @author guqing
 * @since 2.0.0
 */
@Configuration
public class ThemeConfiguration {
    private final HaloProperties haloProperties;

    public ThemeConfiguration(HaloProperties haloProperties) {
        this.haloProperties = haloProperties;
    }

    @Bean
    public RouterFunction<ServerResponse> themeAssets(WebProperties webProperties) {
        var cacheProperties = webProperties.getResources().getCache();
        return route(
            GET("/themes/{themeName}/assets/{*resource}").and(accept(MediaType.TEXT_PLAIN)),
            request -> {
                var themeName = request.pathVariable("themeName");
                var resource = request.pathVariable("resource");
                var fsRes = new FileSystemResource(getThemeAssetsPath(themeName, resource));
                var bodyBuilder = ServerResponse.ok()
                    .cacheControl(cacheProperties.getCachecontrol().toHttpCacheControl());
                try {
                    if (cacheProperties.isUseLastModified()) {
                        var lastModified = Instant.ofEpochMilli(fsRes.lastModified());
                        return request.checkNotModified(lastModified)
                            .switchIfEmpty(Mono.defer(() -> bodyBuilder.lastModified(lastModified)
                                .body(BodyInserters.fromResource(fsRes))));
                    }
                    return bodyBuilder.body(BodyInserters.fromResource(fsRes));
                } catch (IOException e) {
                    return Mono.error(e);
                }
            });
    }

    private Path getThemeAssetsPath(String themeName, String resource) {
        return FilePathUtils.combinePath(haloProperties.getWorkDir().toString(),
            "themes", themeName, "templates", "assets", resource);
    }

    @Bean
    LinkExpressionObjectDialect linkExpressionObjectDialect() {
        return new LinkExpressionObjectDialect();
    }
}
