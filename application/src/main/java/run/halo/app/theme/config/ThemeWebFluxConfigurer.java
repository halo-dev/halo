package run.halo.app.theme.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.resource.AbstractResourceResolver;
import org.springframework.web.reactive.resource.EncodedResourceResolver;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.reactive.resource.ResourceResolverChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.infra.exception.AccessDeniedException;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.theme.ThemeScreenshots;
import run.halo.app.theme.ThemeUiResources;

@Component
public class ThemeWebFluxConfigurer implements WebFluxConfigurer {

    private static final String THEME_NAME_VARIABLE = "themeName";
    private static final String RESOURCE_PATHS_VARIABLE = "resourcePaths";

    private final ThemeRootGetter themeRootGetter;

    private final WebProperties.Resources resourcesProperties;

    public ThemeWebFluxConfigurer(ThemeRootGetter themeRootGetter, WebProperties webProperties) {
        this.themeRootGetter = themeRootGetter;
        this.resourcesProperties = webProperties.getResources();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        var cacheControl = resourcesProperties.getCache().getCachecontrol().toHttpCacheControl();
        if (cacheControl == null) {
            cacheControl = CacheControl.empty();
        }
        var useLastModified = resourcesProperties.getCache().isUseLastModified();
        registry.addResourceHandler("/themes/{themeName}/screenshot.{extension}")
                .setCacheControl(cacheControl)
                .setUseLastModified(useLastModified)
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver())
                .addResolver(new ThemeScreenshotResourceResolver(themeRootGetter.get()));
        registry.addResourceHandler("/themes/{themeName}/ui-plugin/assets/{*resourcePaths}")
                .setCacheControl(cacheControl)
                .setUseLastModified(useLastModified)
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver())
                .addResolver(new ThemeUiResourceResolver(themeRootGetter.get()));
        registry.addResourceHandler("/themes/{themeName}/assets/{*resourcePaths}")
                .setCacheControl(cacheControl)
                .setUseLastModified(useLastModified)
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver())
                .addResolver(new ThemePathResourceResolver(themeRootGetter.get()));
    }

    /**
     * Theme path resource resolver. The resolver is used to resolve theme assets from the request path.
     *
     * @author johnniang
     */
    private static class ThemePathResourceResolver extends AbstractResourceResolver {

        private final Path themeRoot;

        private ThemePathResourceResolver(Path themeRoot) {
            this.themeRoot = themeRoot;
        }

        @Override
        protected Mono<Resource> resolveResourceInternal(
                ServerWebExchange exchange,
                String requestPath,
                List<? extends Resource> locations,
                ResourceResolverChain chain) {
            if (exchange == null) {
                return Mono.empty();
            }
            Map<String, String> requiredAttribute =
                    exchange.getRequiredAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            var themeName = requiredAttribute.get(THEME_NAME_VARIABLE);
            var resourcePaths = requiredAttribute.get(RESOURCE_PATHS_VARIABLE);

            if (StringUtils.isAnyBlank(themeName, resourcePaths)) {
                return Mono.empty();
            }

            var assetsPath = themeRoot.resolve(themeName + "/templates/assets/" + resourcePaths);
            FileUtils.checkDirectoryTraversal(themeRoot, assetsPath);
            var location = new FileSystemResource(assetsPath);
            if (!location.isReadable()) {
                return Mono.empty();
            }
            return Mono.just(location);
        }

        @Override
        protected Mono<String> resolveUrlPathInternal(
                String resourceUrlPath, List<? extends Resource> locations, ResourceResolverChain chain) {
            throw new UnsupportedOperationException();
        }
    }

    private static class ThemeUiResourceResolver extends AbstractResourceResolver {

        private final Path themeRoot;

        private ThemeUiResourceResolver(Path themeRoot) {
            this.themeRoot = themeRoot;
        }

        @Override
        protected Mono<Resource> resolveResourceInternal(
                ServerWebExchange exchange,
                String requestPath,
                List<? extends Resource> locations,
                ResourceResolverChain chain) {
            if (exchange == null) {
                return Mono.empty();
            }
            Map<String, String> requiredAttribute =
                    exchange.getRequiredAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            var themeName = requiredAttribute.get(THEME_NAME_VARIABLE);
            var resourcePaths = requiredAttribute.get(RESOURCE_PATHS_VARIABLE);
            if (StringUtils.isAnyBlank(themeName, resourcePaths)) {
                return Mono.empty();
            }
            var resource = ThemeUiResources.getResource(themeRoot, themeName, resourcePaths);
            if (resource == null) {
                return Mono.error(
                        new NoResourceFoundException(exchange.getRequest().getURI(), resourcePaths));
            }
            return Mono.just(resource);
        }

        @Override
        protected Mono<String> resolveUrlPathInternal(
                String resourceUrlPath, List<? extends Resource> locations, ResourceResolverChain chain) {
            throw new UnsupportedOperationException();
        }
    }

    /** Theme screenshot resource resolver. The resolver only exposes supported screenshot files from the theme root. */
    private static class ThemeScreenshotResourceResolver extends AbstractResourceResolver {

        private final Path themeRoot;

        private ThemeScreenshotResourceResolver(Path themeRoot) {
            this.themeRoot = themeRoot;
        }

        @Override
        protected Mono<Resource> resolveResourceInternal(
                ServerWebExchange exchange,
                String requestPath,
                List<? extends Resource> locations,
                ResourceResolverChain chain) {
            if (exchange == null) {
                return Mono.empty();
            }
            Map<String, String> requiredAttribute =
                    exchange.getRequiredAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            var themeName = requiredAttribute.get("themeName");
            var extension = requiredAttribute.get("extension");
            var filename = "screenshot." + extension;

            if (StringUtils.isAnyBlank(themeName, extension) || !ThemeScreenshots.isSupportedFilename(filename)) {
                return Mono.empty();
            }

            var screenshotPath = themeRoot.resolve(themeName).resolve(filename);
            try {
                FileUtils.checkDirectoryTraversal(themeRoot, screenshotPath);
            } catch (AccessDeniedException e) {
                return Mono.empty();
            }
            if (!Files.isRegularFile(screenshotPath) || !Files.isReadable(screenshotPath)) {
                return Mono.empty();
            }
            return Mono.just(new FileSystemResource(screenshotPath));
        }

        @Override
        protected Mono<String> resolveUrlPathInternal(
                String resourceUrlPath, List<? extends Resource> locations, ResourceResolverChain chain) {
            throw new UnsupportedOperationException();
        }
    }
}
