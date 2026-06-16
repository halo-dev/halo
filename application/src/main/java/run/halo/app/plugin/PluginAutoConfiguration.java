package run.halo.app.plugin;

import static run.halo.app.plugin.resources.BundleResourceUtils.CONSOLE_BUNDLE_LOCATION;
import static run.halo.app.plugin.resources.BundleResourceUtils.UI_BUNDLE_LOCATION;
import static run.halo.app.plugin.resources.BundleResourceUtils.getBundleResource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemVersionSupplier;

/**
 * Plugin autoconfiguration for Spring Boot.
 *
 * @author guqing
 * @see PluginProperties
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(PluginProperties.class)
public class PluginAutoConfiguration {

    @Bean
    public PluginRequestMappingHandlerMapping pluginRequestMappingHandlerMapping(
            @Qualifier("webFluxContentTypeResolver") RequestedContentTypeResolver requestedContentTypeResolver) {
        PluginRequestMappingHandlerMapping mapping = new PluginRequestMappingHandlerMapping();
        mapping.setContentTypeResolver(requestedContentTypeResolver);
        mapping.setOrder(-1);
        return mapping;
    }

    @Bean
    public SpringPluginManager pluginManager(
            ApplicationContext context,
            SystemVersionSupplier systemVersionSupplier,
            PluginProperties pluginProperties,
            PluginsRootGetter pluginsRootGetter)
            throws FileNotFoundException, URISyntaxException {
        return new HaloPluginManager(context, pluginProperties, systemVersionSupplier, pluginsRootGetter);
    }

    @Bean
    public ScheduledTaskHolder pluginScheduledTaskHolder(SpringPluginManager pluginManager) {
        return new PluginScheduledTaskHolder(pluginManager);
    }

    @Bean
    public RouterFunction<ServerResponse> pluginJsBundleRoute(
            PluginManager pluginManager, WebProperties webProperties) {
        var cacheProperties = webProperties.getResources().getCache();
        return RouterFunctions.route()
                .GET(
                        "/plugins/{name}/assets/ui/{*resource}",
                        request -> getResourceResponse(pluginManager, cacheProperties, UI_BUNDLE_LOCATION, request))
                .GET(
                        "/plugins/{name}/assets/console/{*resource}",
                        request ->
                                getResourceResponse(pluginManager, cacheProperties, CONSOLE_BUNDLE_LOCATION, request))
                .build();
    }

    private Mono<ServerResponse> getResourceResponse(
            PluginManager pluginManager,
            WebProperties.Resources.Cache cacheProperties,
            String bundleLocation,
            ServerRequest request) {
        String pluginName = request.pathVariable("name");
        String fileName = request.pathVariable("resource");

        var resource = getBundleResource(pluginManager, pluginName, bundleLocation, fileName);
        if (resource == null || !resource.exists()) {
            return ServerResponse.notFound().build();
        }
        var useLastModified = cacheProperties.isUseLastModified();
        var cacheControl = cacheProperties.getCachecontrol().toHttpCacheControl();
        if (cacheControl == null) {
            cacheControl = CacheControl.empty();
        }
        var bodyBuilder = ServerResponse.ok().cacheControl(cacheControl);
        try {
            if (useLastModified) {
                var lastModified = Instant.ofEpochMilli(resource.lastModified());
                return request.checkNotModified(lastModified)
                        .switchIfEmpty(Mono.defer(() ->
                                bodyBuilder.lastModified(lastModified).body(BodyInserters.fromResource(resource))));
            }
            return bodyBuilder.body(BodyInserters.fromResource(resource));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
