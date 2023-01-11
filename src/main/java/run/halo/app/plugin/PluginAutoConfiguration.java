package run.halo.app.plugin;

import static run.halo.app.plugin.resources.BundleResourceUtils.getJsBundleResource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.ClassLoadingStrategy;
import org.pf4j.CompoundPluginLoader;
import org.pf4j.CompoundPluginRepository;
import org.pf4j.DefaultPluginRepository;
import org.pf4j.DevelopmentPluginLoader;
import org.pf4j.JarPluginLoader;
import org.pf4j.JarPluginRepository;
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginLoader;
import org.pf4j.PluginManager;
import org.pf4j.PluginRepository;
import org.pf4j.PluginStatusProvider;
import org.pf4j.RuntimeMode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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

    private final PluginProperties pluginProperties;

    private final SystemVersionSupplier systemVersionSupplier;

    @Qualifier("webFluxContentTypeResolver")
    private final RequestedContentTypeResolver requestedContentTypeResolver;

    public PluginAutoConfiguration(PluginProperties pluginProperties,
        SystemVersionSupplier systemVersionSupplier,
        RequestedContentTypeResolver requestedContentTypeResolver) {
        this.pluginProperties = pluginProperties;
        this.systemVersionSupplier = systemVersionSupplier;
        this.requestedContentTypeResolver = requestedContentTypeResolver;
    }

    @Bean
    public PluginRequestMappingHandlerMapping pluginRequestMappingHandlerMapping() {
        PluginRequestMappingHandlerMapping mapping = new PluginRequestMappingHandlerMapping();
        mapping.setContentTypeResolver(requestedContentTypeResolver);
        mapping.setOrder(-1);
        return mapping;
    }

    @Bean
    public PluginRequestMappingManager pluginRequestMappingManager() {
        return new PluginRequestMappingManager(pluginRequestMappingHandlerMapping());
    }

    @Bean
    public HaloPluginManager pluginManager() {
        // Setup RuntimeMode
        System.setProperty("pf4j.mode", pluginProperties.getRuntimeMode().toString());

        // Setup Plugin folder
        String pluginsRoot =
            StringUtils.defaultString(pluginProperties.getPluginsRoot(), "plugins");

        System.setProperty("pf4j.pluginsDir", pluginsRoot);
        String appHome = System.getProperty("app.home");
        if (RuntimeMode.DEPLOYMENT == pluginProperties.getRuntimeMode()
            && StringUtils.isNotBlank(appHome)) {
            System.setProperty("pf4j.pluginsDir", appHome + File.separator + pluginsRoot);
        }

        HaloPluginManager pluginManager =
            new HaloPluginManager(new File(pluginsRoot).toPath()) {
                @Override
                protected PluginLoader createPluginLoader() {
                    if (pluginProperties.getCustomPluginLoader() != null) {
                        Class<PluginLoader> clazz = pluginProperties.getCustomPluginLoader();
                        try {
                            Constructor<?> constructor = clazz.getConstructor(PluginManager.class);
                            return (PluginLoader) constructor.newInstance(this);
                        } catch (Exception ex) {
                            throw new IllegalArgumentException(
                                String.format("Create custom PluginLoader %s failed. Make sure"
                                        + "there is a constructor with one argument that accepts "
                                        + "PluginLoader",
                                    clazz.getName()));
                        }
                    } else {
                        return new CompoundPluginLoader()
                            .add(new DevelopmentPluginLoader(this) {

                                @Override
                                protected PluginClassLoader createPluginClassLoader(Path pluginPath,
                                    PluginDescriptor pluginDescriptor) {
                                    return new PluginClassLoader(pluginManager, pluginDescriptor,
                                        getClass().getClassLoader(), ClassLoadingStrategy.APD);
                                }

                                @Override
                                public ClassLoader loadPlugin(Path pluginPath,
                                    PluginDescriptor pluginDescriptor) {
                                    if (pluginProperties.getClassesDirectories() != null) {
                                        for (String classesDirectory :
                                            pluginProperties.getClassesDirectories()) {
                                            pluginClasspath.addClassesDirectories(classesDirectory);
                                        }
                                    }
                                    if (pluginProperties.getLibDirectories() != null) {
                                        for (String libDirectory :
                                            pluginProperties.getLibDirectories()) {
                                            pluginClasspath.addJarsDirectories(libDirectory);
                                        }
                                    }
                                    return super.loadPlugin(pluginPath, pluginDescriptor);
                                }
                            }, this::isDevelopment)
                            .add(new JarPluginLoader(this) {
                                @Override
                                public ClassLoader loadPlugin(Path pluginPath,
                                    PluginDescriptor pluginDescriptor) {
                                    PluginClassLoader pluginClassLoader =
                                        new PluginClassLoader(pluginManager, pluginDescriptor,
                                            getClass().getClassLoader(), ClassLoadingStrategy.APD);
                                    pluginClassLoader.addFile(pluginPath.toFile());
                                    return pluginClassLoader;

                                }
                            }, this::isNotDevelopment);
                    }
                }

                @Override
                protected PluginStatusProvider createPluginStatusProvider() {
                    if (PropertyPluginStatusProvider.isPropertySet(pluginProperties)) {
                        return new PropertyPluginStatusProvider(pluginProperties);
                    }
                    return super.createPluginStatusProvider();
                }

                @Override
                protected PluginRepository createPluginRepository() {
                    var developmentPluginRepository =
                        new DefaultDevelopmentPluginRepository(getPluginsRoots());
                    developmentPluginRepository
                        .setFixedPaths(pluginProperties.getFixedPluginPath());
                    return new CompoundPluginRepository()
                        .add(developmentPluginRepository, this::isDevelopment)
                        .add(new JarPluginRepository(getPluginsRoots()), this::isNotDevelopment)
                        .add(new DefaultPluginRepository(getPluginsRoots()),
                            this::isNotDevelopment);
                }
            };

        pluginManager.setExactVersionAllowed(pluginProperties.isExactVersionAllowed());
        // only for development mode
        if (RuntimeMode.DEPLOYMENT.equals(pluginManager.getRuntimeMode())) {
            pluginManager.setSystemVersion(getSystemVersion());
        }
        return pluginManager;
    }

    String getSystemVersion() {
        return systemVersionSupplier.get().getNormalVersion();
    }

    @Bean
    public RouterFunction<ServerResponse> pluginJsBundleRoute(HaloPluginManager haloPluginManager,
        WebProperties webProperties) {
        var cacheProperties = webProperties.getResources().getCache();
        return RouterFunctions.route()
            .GET("/plugins/{name}/assets/console/{*resource}", request -> {
                String pluginName = request.pathVariable("name");
                String fileName = request.pathVariable("resource");

                var jsBundle = getJsBundleResource(haloPluginManager, pluginName, fileName);
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
