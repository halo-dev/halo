package run.halo.app.theme;

import java.nio.file.Path;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring6.ISpringWebFluxTemplateEngine;
import org.thymeleaf.templateresolver.ITemplateResolver;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.FilePathUtils;
import run.halo.app.theme.cache.ThemeCacheManager;
import run.halo.app.theme.engine.ThemeWebFluxTemplateEngine;
import run.halo.app.theme.message.DelegatingThemeMessageResolver;

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
    ISpringWebFluxTemplateEngine templateEngine(ThymeleafProperties properties,
        ObjectProvider<ITemplateResolver> templateResolvers,
        ObjectProvider<IDialect> dialects) {
        ThemeWebFluxTemplateEngine engine = new ThemeWebFluxTemplateEngine();
        engine.setEnableSpringELCompiler(properties.isEnableSpringElCompiler());
        engine.setMessageResolver(new DelegatingThemeMessageResolver());
        engine.setLinkBuilder(new ThemeLinkBuilder());

        engine.setRenderHiddenMarkersBeforeCheckboxes(
            properties.isRenderHiddenMarkersBeforeCheckboxes());

        templateResolvers.orderedStream().forEach(engine::addTemplateResolver);
        dialects.orderedStream().forEach(engine::addDialect);

        engine.setTemplateResolver(themeResourceTemplateResolver());
        engine.setCacheManager(new ThemeCacheManager());
        return engine;
    }

    @Bean
    ThemeResourceTemplateResolver themeResourceTemplateResolver() {
        ThemeResourceTemplateResolver themeResourceTemplateResolver =
            new ThemeResourceTemplateResolver();
        themeResourceTemplateResolver.setSuffix(".html");
        return themeResourceTemplateResolver;
    }

    @Bean
    public RouterFunction<ServerResponse> themeAssets() {
        return RouterFunctions
            .route(RequestPredicates.GET("/themes/{themeName}/assets/{*resource}")
                    .and(RequestPredicates.accept(MediaType.TEXT_HTML)),
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
            .route(RequestPredicates.GET("/").or(RequestPredicates.GET("/index"))
                    .and(RequestPredicates.accept(MediaType.TEXT_HTML)),
                request -> ServerResponse.ok().render("index"));
    }

    private Path getThemeAssetsPath(String themeName, String resource) {
        return FilePathUtils.combinePath(haloProperties.getWorkDir().toString(),
            "themes", themeName, "templates", "assets", resource);
    }
}
