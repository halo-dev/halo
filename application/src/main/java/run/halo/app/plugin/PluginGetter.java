package run.halo.app.plugin;

import run.halo.app.core.extension.Plugin;
import run.halo.app.infra.exception.NotFoundException;

/**
 * An interface to get {@link Plugin} by name.
 *
 * @author guqing
 * @since 2.17.0
 */
@FunctionalInterface
public interface PluginGetter {

    /**
     * Get plugin by name.
     *
     * @param name plugin name must not be null
     * @return plugin
     * @throws IllegalArgumentException if plugin name is null
     * @throws NotFoundException    if plugin not found
     */
    Plugin getPlugin(String name);
}
