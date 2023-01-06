package run.halo.app.plugin;

import static run.halo.app.plugin.resources.BundleResourceUtils.getJsBundleResource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.time.Duration;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

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

    @Nullable
    private BuildProperties buildProperties;

    @Qualifier("webFluxContentTypeResolver")
    private final RequestedContentTypeResolver requestedContentTypeResolver;

    public PluginAutoConfiguration(PluginProperties pluginProperties,
        RequestedContentTypeResolver requestedContentTypeResolver) {
        this.pluginProperties = pluginProperties;
        this.requestedContentTypeResolver = requestedContentTypeResolver;
    }

    @Autowired(required = false)
    public void setBuildProperties(@Nullable BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
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
                                public ClassLoader loadPlugin(Path pluginPath,
                                    PluginDescriptor pluginDescriptor) {
                                    PluginClassLoader pluginClassLoader =
                                        new PluginClassLoader(pluginManager, pluginDescriptor,
                                            getClass().getClassLoader(), ClassLoadingStrategy.APD);

                                    loadClasses(pluginPath, pluginClassLoader);
                                    loadJars(pluginPath, pluginClassLoader);

                                    return pluginClassLoader;
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
        String defaultVersion = "0.0.0";
        if (buildProperties == null) {
            return defaultVersion;
        }
        return StringUtils.defaultString(buildProperties.getVersion(), defaultVersion);
    }

    @Bean
    public RouterFunction<ServerResponse> pluginJsBundleRoute(HaloPluginManager haloPluginManager) {
        return RouterFunctions.route()
            .GET("/plugins/{name}/assets/console/{*resource}", request -> {
                String pluginName = request.pathVariable("name");
                String fileName = request.pathVariable("resource");

                var jsBundle = getJsBundleResource(haloPluginManager, pluginName, fileName);
                if (jsBundle == null || !jsBundle.exists()) {
                    return ServerResponse.notFound().build();
                }
                try {
                    var lastModified = Instant.ofEpochMilli(jsBundle.lastModified());
                    return request.checkNotModified(lastModified)
                        .switchIfEmpty(Mono.defer(() -> ServerResponse.ok()
                                .cacheControl(CacheControl.maxAge(Duration.ofDays(365 / 2)))
                                .lastModified(lastModified)
                                .body(BodyInserters.fromResource(jsBundle))));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .build();
    }
}
