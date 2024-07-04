package run.halo.app.plugin;

import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * An interface to get the root path of plugins.
 *
 * @author johnniang
 * @since 2.18.0
 */
public interface PluginsRootGetter extends Supplier<Path> {

}
