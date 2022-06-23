package run.halo.app.plugin;

import org.pf4j.PluginRuntimeException;

/**
 * Exception for plugin not found.
 *
 * @author guqing
 * @since 2.0.0
 */
public class PluginNotFoundException extends PluginRuntimeException {
    public PluginNotFoundException(String message) {
        super(message);
    }

    public PluginNotFoundException(Throwable cause) {
        super(cause);
    }
}
