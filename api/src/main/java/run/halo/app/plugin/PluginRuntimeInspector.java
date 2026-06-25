package run.halo.app.plugin;

import java.util.List;

/**
 * Read-only inspector for plugin runtime diagnostics.
 *
 * <p>This API is intended for monitoring and troubleshooting plugins without exposing plugin
 * lifecycle operations or mutable runtime internals.
 *
 * @author webjing
 * @since 2.25.0
 */
public interface PluginRuntimeInspector {

    /**
     * Lists runtime diagnostics for started plugins.
     *
     * @return read-only runtime diagnostics for started plugins
     */
    List<PluginRuntimeInfo> list();
}
