package run.halo.app.theme;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import java.nio.file.Path;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.FilePathUtils;
import run.halo.app.theme.dialect.LinkExpressionObjectDialect;
import run.halo.app.theme.dialect.ThemeJava8TimeDialect;

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
    public RouterFunction<ServerResponse> themeAssets() {
        return RouterFunctions
            .route(GET("/themes/{themeName}/assets/{*resource}")
                    .and(accept(MediaType.TEXT_HTML)),
                request -> {
                    String themeName = request.pathVariable("themeName");
                    String resource = request.pathVariable("resource");
                    FileSystemResource fileSystemResource =
                        new FileSystemResource(getThemeAssetsPath(themeName, resource));
                    return ServerResponse.ok()
                        .bodyValue(fileSystemResource);
                });
    }

    private Path getThemeAssetsPath(String themeName, String resource) {
        return FilePathUtils.combinePath(haloProperties.getWorkDir().toString(),
            "themes", themeName, "templates", "assets", resource);
    }

    @Bean
    Java8TimeDialect java8TimeDialect() {
        return new ThemeJava8TimeDialect();
    }

    @Bean
    LinkExpressionObjectDialect linkExpressionObjectDialect() {
        return new LinkExpressionObjectDialect();
    }
}
