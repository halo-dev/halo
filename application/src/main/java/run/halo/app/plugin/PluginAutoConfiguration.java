package run.halo.app.plugin;

import static run.halo.app.plugin.resources.BundleResourceUtils.getJsBundleResource;

import java.io.IOException;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
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
        @Qualifier("webFluxContentTypeResolver")
        RequestedContentTypeResolver requestedContentTypeResolver
    ) {
        PluginRequestMappingHandlerMapping mapping = new PluginRequestMappingHandlerMapping();
        mapping.setContentTypeResolver(requestedContentTypeResolver);
        mapping.setOrder(-1);
        return mapping;
    }

    @Bean
    public SpringPluginManager pluginManager(ApplicationContext context,
        SystemVersionSupplier systemVersionSupplier,
        PluginProperties pluginProperties,
        PluginsRootGetter pluginsRootGetter) {
        return new HaloPluginManager(
            context, pluginProperties, systemVersionSupplier, pluginsRootGetter
        );
    }

    @Bean
    public RouterFunction<ServerResponse> pluginJsBundleRoute(PluginManager pluginManager,
        WebProperties webProperties) {
        var cacheProperties = webProperties.getResources().getCache();
        return RouterFunctions.route()
            .GET("/plugins/{name}/assets/console/{*resource}", request -> {
                String pluginName = request.pathVariable("name");
                String fileName = request.pathVariable("resource");

                var jsBundle = getJsBundleResource(pluginManager, pluginName, fileName);
                if (jsBundle == null || !jsBundle.exists()) {
                    return ServerResponse.notFound().build();
                }
                var useLastModified = cacheProperties.isUseLastModified();
                var bodyBuilder = ServerResponse.ok()
                    .cacheControl(cacheProperties.getCachecontrol().toHttpCacheControl());
                try {
                    if (useLastModified) {
                        var lastModified = Instant.ofEpochMilli(jsBundle.lastModified());
                        return request.checkNotModified(lastModified)
                            .switchIfEmpty(Mono.defer(() -> bodyBuilder.lastModified(lastModified)
                                .body(BodyInserters.fromResource(jsBundle))));
                    }
                    return bodyBuilder.body(BodyInserters.fromResource(jsBundle));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .build();
    }
}
