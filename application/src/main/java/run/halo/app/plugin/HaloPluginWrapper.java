package run.halo.app.plugin;

import java.nio.file.Files;
import java.nio.file.Path;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;

/**
 * A wrapper over plugin instance for Halo.
 *
 * @author guqing
 * @since 2.10.0
 */
public class HaloPluginWrapper extends PluginWrapper {

    private final RuntimeMode runtimeMode;

    /**
     * Creates a new plugin wrapper to manage the specified plugin.
     */
    public HaloPluginWrapper(PluginManager pluginManager, PluginDescriptor descriptor,
        Path pluginPath, ClassLoader pluginClassLoader) {
        super(pluginManager, descriptor, pluginPath, pluginClassLoader);
        this.runtimeMode = Files.isDirectory(pluginPath)
            ? RuntimeMode.DEVELOPMENT : RuntimeMode.DEPLOYMENT;
    }

    @Override
    public RuntimeMode getRuntimeMode() {
        return runtimeMode;
    }
}
