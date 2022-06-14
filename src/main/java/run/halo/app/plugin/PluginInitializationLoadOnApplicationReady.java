package run.halo.app.plugin;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Load plugins after application ready.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PluginInitializationLoadOnApplicationReady
    implements ApplicationListener<ApplicationReadyEvent> {

    private final HaloPluginManager haloPluginManager;

    public PluginInitializationLoadOnApplicationReady(HaloPluginManager haloPluginManager) {
        this.haloPluginManager = haloPluginManager;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        haloPluginManager.loadPlugins();
    }
}
