package run.halo.app.plugin;

import lombok.extern.slf4j.Slf4j;
import org.pf4j.Plugin;
import org.pf4j.PluginFactory;
import org.pf4j.PluginWrapper;

/**
 * The default implementation for PluginFactory.
 * <p>Get a {@link BasePlugin} instance from the {@link PluginApplicationContext}.</p>
 *
 * @author guqing
 * @author johnniang
 * @since 2.0.0
 */
@Slf4j
public class SpringPluginFactory implements PluginFactory {


    private final PluginApplicationContextFactory contextFactory;

    public SpringPluginFactory(PluginApplicationContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    @Override
    public Plugin create(PluginWrapper pluginWrapper) {
        var pluginContext = new PluginContext(
            pluginWrapper.getPluginId(),
            pluginWrapper.getDescriptor().getVersion(),
            pluginWrapper.getRuntimeMode()
        );
        return new SpringPlugin(
            contextFactory,
            pluginContext
        );
    }

}
