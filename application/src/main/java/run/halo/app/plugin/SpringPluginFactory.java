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
    private final PluginGetter pluginGetter;

    public SpringPluginFactory(PluginApplicationContextFactory contextFactory,
        PluginGetter pluginGetter) {
        this.contextFactory = contextFactory;
        this.pluginGetter = pluginGetter;
    }

    @Override
    public Plugin create(PluginWrapper pluginWrapper) {
        var plugin = pluginGetter.getPlugin(pluginWrapper.getPluginId());
        var pluginContext = PluginContext.builder()
            .name(pluginWrapper.getPluginId())
            .configMapName(plugin.getSpec().getConfigMapName())
            .version(pluginWrapper.getDescriptor().getVersion())
            .runtimeMode(pluginWrapper.getRuntimeMode())
            .build();
        return new SpringPlugin(
            contextFactory,
            pluginContext
        );
    }
}
