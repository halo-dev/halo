package run.halo.app.plugin;

import java.util.List;

/**
 * Service for plugin.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface PluginService {

    /**
     * Lists plugin from repository.
     *
     * @return all loaded plugins.
     */
    List<Plugin> list();
}
