package run.halo.app.theme.config;

import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.resource.AbstractResourceResolver;
import org.springframework.web.reactive.resource.EncodedResourceResolver;
import org.springframework.web.reactive.resource.ResourceResolverChain;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.infra.utils.FileUtils;

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
            .addResolver(new ThemePathResourceResolver(themeRootGetter.get()));
    }

    @Bean
    RouterFunction<ServerResponse> localeRoute() {
        return RouterFunctions.route()
            .POST(
                "/locale",
                contentType(MediaType.APPLICATION_FORM_URLENCODED),
                request -> {
                    var location =
                        Optional.ofNullable(request.headers().firstHeader(HttpHeaders.REFERER))
                            .filter(StringUtils::isNotBlank)
                            .map(URI::create)
                            .orElseGet(() -> URI.create("/"));
                    return request.formData()
                        .flatMap(formData -> Optional.ofNullable(formData.getFirst("language"))
                            .filter(StringUtils::isNotBlank)
                            .map(Locale::forLanguageTag)
                            .map(locale -> ServerResponse.status(HttpStatus.FOUND)
                                .location(location)
                                .cookie(ResponseCookie.from("language", locale.toLanguageTag())
                                    .maxAge(Duration.ofDays(365))
                                    .httpOnly(false)
                                    .path("/")
                                    .build()
                                )
                                .build()
                            )
                            .orElseGet(
                                () -> Mono.error(
                                    new ServerWebInputException("Invalid language tag"))
                            )
                        );
                })
            .build();
    }

    /**
     * Theme path resource resolver. The resolver is used to resolve theme assets from the request
     * path.
     *
     * @author johnniang
     */
    private static class ThemePathResourceResolver extends AbstractResourceResolver {

        private final Path themeRoot;

        private ThemePathResourceResolver(Path themeRoot) {
            this.themeRoot = themeRoot;
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

            var assetsPath = themeRoot.resolve(themeName + "/templates/assets/" + resourcePaths);
            FileUtils.checkDirectoryTraversal(themeRoot, assetsPath);
            var location = new FileSystemResource(assetsPath);
            if (!location.isReadable()) {
                return Mono.empty();
            }
            return Mono.just(location);
        }

        @Override
        protected Mono<String> resolveUrlPathInternal(String resourceUrlPath,
            List<? extends Resource> locations, ResourceResolverChain chain) {
            throw new UnsupportedOperationException();
        }

    }
}
