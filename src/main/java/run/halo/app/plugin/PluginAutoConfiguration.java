package run.halo.app.plugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.ClassLoadingStrategy;
import org.pf4j.CompoundPluginLoader;
import org.pf4j.DevelopmentPluginLoader;
import org.pf4j.JarPluginLoader;
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginLoader;
import org.pf4j.PluginManager;
import org.pf4j.PluginStatusProvider;
import org.pf4j.RuntimeMode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;

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
    @Qualifier("webFluxContentTypeResolver")
    private final RequestedContentTypeResolver requestedContentTypeResolver;

    public PluginAutoConfiguration(PluginProperties pluginProperties,
        RequestedContentTypeResolver requestedContentTypeResolver) {
        this.pluginProperties = pluginProperties;
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
            StringUtils.hasText(pluginProperties.getPluginsRoot())
                ? pluginProperties.getPluginsRoot()
                : "plugins";
        System.setProperty("pf4j.pluginsDir", pluginsRoot);
        String appHome = System.getProperty("app.home");
        if (RuntimeMode.DEPLOYMENT == pluginProperties.getRuntimeMode()
            && StringUtils.hasText(appHome)) {
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
            };

        pluginManager.setExactVersionAllowed(pluginProperties.isExactVersionAllowed());
        pluginManager.setSystemVersion(pluginProperties.getSystemVersion());

        return pluginManager;
    }
}
