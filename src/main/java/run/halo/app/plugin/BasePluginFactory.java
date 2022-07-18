package run.halo.app.plugin;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Plugin;
import org.pf4j.PluginFactory;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * The default implementation for PluginFactory.
 * <p>Get a {@link BasePlugin} instance from the {@link PluginApplicationContext}.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class BasePluginFactory implements PluginFactory {

    @Override
    public Plugin create(PluginWrapper pluginWrapper) {
        return getPluginContext(pluginWrapper)
            .map(context -> {
                try {
                    return context.getBean(BasePlugin.class);
                } catch (NoSuchBeanDefinitionException e) {
                    log.info(
                        "No bean named 'basePlugin' found in the context create default instance");
                    DefaultListableBeanFactory beanFactory =
                        context.getDefaultListableBeanFactory();
                    BasePlugin pluginInstance = new BasePlugin(pluginWrapper);
                    beanFactory.registerSingleton(Plugin.class.getName(), pluginInstance);
                    return pluginInstance;
                }
            })
            .orElse(null);
    }

    private Optional<PluginApplicationContext> getPluginContext(PluginWrapper pluginWrapper) {
        try {
            return Optional.of(ExtensionContextRegistry.getInstance())
                .map(registry -> registry.getByPluginId(pluginWrapper.getPluginId()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
