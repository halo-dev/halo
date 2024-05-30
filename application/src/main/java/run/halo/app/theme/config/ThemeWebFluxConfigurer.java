package run.halo.app.theme.config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.resource.AbstractResourceResolver;
import org.springframework.web.reactive.resource.EncodedResourceResolver;
import org.springframework.web.reactive.resource.PathResourceResolver;
import org.springframework.web.reactive.resource.ResourceResolverChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.infra.ThemeRootGetter;

@Component
public class ThemeWebFluxConfigurer implements WebFluxConfigurer {

    private final ThemeRootGetter themeRootGetter;

    private final WebProperties.Resources resourcesProperties;

    public ThemeWebFluxConfigurer(ThemeRootGetter themeRootGetter,
        WebProperties webProperties) {
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
        registry.addResourceHandler("/themes/{themeName}/assets/{*resourcePaths}")
            .setCacheControl(cacheControl)
            .setUseLastModified(useLastModified)
            .resourceChain(true)
            .addResolver(new EncodedResourceResolver())
            .addResolver(new ThemePathResourceResolver(themeRootGetter.get()))
            .addResolver(new PathResourceResolver());
    }

    /**
     * Theme path resource resolver. The resolver is used to resolve theme assets from the request
     * path.
     *
     * @author johnniang
     */
    private static class ThemePathResourceResolver extends AbstractResourceResolver {

        private final Resource themeRootResource;

        private ThemePathResourceResolver(Path themeRoot) {
            try {
                this.themeRootResource = new FileUrlResource(themeRoot.toUri().toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException("Failed to resolve " + themeRoot + " to URL.", e);
            }
        }

        @Override
        protected Mono<Resource> resolveResourceInternal(ServerWebExchange exchange,
            String requestPath, List<? extends Resource> locations, ResourceResolverChain chain) {
            if (exchange == null) {
                return Mono.empty();
            }
            Map<String, String> requiredAttribute =
                exchange.getRequiredAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            var themeName = requiredAttribute.get("themeName");
            var resourcePaths = requiredAttribute.get("resourcePaths");

            if (StringUtils.isAnyBlank(themeName, resourcePaths)) {
                return Mono.empty();
            }

            try {
                var location = themeRootResource.createRelative(themeName + "/templates/assets/");
                return chain.resolveResource(exchange, resourcePaths, List.of(location));
            } catch (IOException e) {
                return Mono.empty();
            }
        }

        @Override
        protected Mono<String> resolveUrlPathInternal(String resourceUrlPath,
            List<? extends Resource> locations,
            ResourceResolverChain chain) {
            return chain.resolveUrlPath(resourceUrlPath, locations);
        }
    }
}
