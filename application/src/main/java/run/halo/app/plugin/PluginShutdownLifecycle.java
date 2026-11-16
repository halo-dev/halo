package run.halo.app.plugin;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import run.halo.app.infra.InitializationPhase;

/** Stops plugins while core controllers can still finish deleting their bundled resources. */
@Component
public class PluginShutdownLifecycle implements SmartLifecycle {
    // Resolve lazily so Spring's dependency ordering does not stop controllers before this lifecycle.
    private final ObjectProvider<SpringPluginManager> plugins;
    private volatile boolean running;

    public PluginShutdownLifecycle(ObjectProvider<SpringPluginManager> plugins) {
        this.plugins = plugins;
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        try {
            var manager = plugins.getIfAvailable();
            if (manager != null) {
                manager.stopPlugins();
            }
        } finally {
            running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return InitializationPhase.CONTROLLERS.getPhase() + 1;
    }
}
