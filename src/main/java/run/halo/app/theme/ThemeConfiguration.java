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

    @Bean
    RouterFunction<ServerResponse> routeIndex() {
        return RouterFunctions
            .route(GET("/").or(GET("/index"))
                    .or(GET("/page/{page}"))
                    .and(accept(MediaType.TEXT_HTML)),
                request -> ServerResponse.ok().render("index"));
    }

    @Bean
    RouterFunction<ServerResponse> routePosts() {
        // prefix
        // posts
        return RouterFunctions
            .route(GET("archives")
                    .or(GET("/archives/page/{page}"))
                    .or(GET("/archives/{year}/{month}"))
                    .or(GET("/archives/{year}/{month}/page/{page}"))
                    .and(accept(MediaType.TEXT_HTML)),
                request -> ServerResponse.ok().render("archives"));
    }

    @Bean
    RouterFunction<ServerResponse> routePost() {
        // Map<String, String> patternNameMap
        // /archives/{slug}
        // /archives/{name}
        // /?p={name}
        // /{year}/{month}/{slug}
        // /{year}/{slug}
        // /{year}/{month}/{day}/{slug}

        // /categories/{categorySlug}/{slug}
        // /products/halo/halo-2-0-0-released // /categories/products/jumpserver//halo-2-0-0-released
        return RouterFunctions
            .route(GET("/archives/{slug}")
                    .and(accept(MediaType.TEXT_HTML)),
                request -> ServerResponse.ok().render("post"));
    }

    @Bean
    RouterFunction<ServerResponse> routeCategories() {
        // prefix
        return RouterFunctions
            .route(GET("/categories")
                    .and(accept(MediaType.TEXT_HTML)),
                request -> ServerResponse.ok().render("categories"));
    }

    @Bean
    RouterFunction<ServerResponse> routeCategory() {
        // prefix
        return RouterFunctions
            .route(GET("/categories/{slug}")
                .or(GET("/categories/{slug}/page/{page}"))
                    .and(accept(MediaType.TEXT_HTML)),
                request -> ServerResponse.ok().render("category"));
    }

    @Bean
    RouterFunction<ServerResponse> routeTags() {
        // prefix
        return RouterFunctions
            .route(GET("/tags")
                    .and(accept(MediaType.TEXT_HTML)),
                request -> ServerResponse.ok().render("tags"));
    }

    @Bean
    RouterFunction<ServerResponse> routeTag() {
        return RouterFunctions
            .route(GET("/tags/{slug}")
                    .and(accept(MediaType.TEXT_HTML)),
                request -> ServerResponse.ok().render("tag"));
    }

    private Path getThemeAssetsPath(String themeName, String resource) {
        return FilePathUtils.combinePath(haloProperties.getWorkDir().toString(),
            "themes", themeName, "templates", "assets", resource);
    }

    @Bean
    Java8TimeDialect java8TimeDialect() {
        return new ThemeJava8TimeDialect();
    }
}
