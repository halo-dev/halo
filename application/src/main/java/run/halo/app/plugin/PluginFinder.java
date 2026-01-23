package run.halo.app.plugin;

import java.nio.file.Path;
import org.jspecify.annotations.NonNull;
import org.pf4j.PluginRuntimeException;
import run.halo.app.core.extension.Plugin;

/**
 * The plugin finder to find plugin manifest from given plugin path.
 *
 * @author johnniang
 * @since 2.22.0
 */
public interface PluginFinder {

    /**
     * Finds plugin manifest by given plugin path.
     *
     * @param pluginPath the plugin path
     * @return the found plugin
     * @throws PluginRuntimeException if any error occurs during finding plugin
     */
    @NonNull
    Plugin find(@NonNull Path pluginPath);

}
