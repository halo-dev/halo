package run.halo.app.theme;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import reactor.core.publisher.Mono;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.theme.dialect.HaloSpringSecurityDialect;
import run.halo.app.theme.dialect.LinkExpressionObjectDialect;

/**
 * @author guqing
 * @since 2.0.0
 */
@Configuration
public class ThemeConfiguration {

    private final ThemeRootGetter themeRoot;

    public ThemeConfiguration(ThemeRootGetter themeRoot) {
        this.themeRoot = themeRoot;
    }

    @Bean
    public RouterFunction<ServerResponse> themeAssets(WebProperties webProperties) {
        var cacheProperties = webProperties.getResources().getCache();
        return route(
            GET("/themes/{themeName}/assets/{*resource}").and(accept(MediaType.TEXT_PLAIN)),
            request -> {
                var themeName = request.pathVariable("themeName");
                var resource = request.pathVariable("resource");
                resource = StringUtils.removeStart(resource, "/");
                var fsRes = new FileSystemResource(getThemeAssetsPath(themeName, resource));
                if (!fsRes.exists()) {
                    return ServerResponse.notFound().build();
                }
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
        return themeRoot.get()
            .resolve(themeName)
            .resolve("templates")
            .resolve("assets")
            .resolve(resource);
    }

    @Bean
    LinkExpressionObjectDialect linkExpressionObjectDialect() {
        return new LinkExpressionObjectDialect();
    }

    @Bean
    SpringSecurityDialect springSecurityDialect(
        ServerSecurityContextRepository securityContextRepository) {
        return new HaloSpringSecurityDialect(securityContextRepository);
    }
}
