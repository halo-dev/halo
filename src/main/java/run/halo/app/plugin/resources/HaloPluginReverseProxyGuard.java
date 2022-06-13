package run.halo.app.plugin.resources;

import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import run.halo.app.plugin.event.HaloPluginStartedEvent;
import run.halo.app.plugin.event.HaloPluginStoppedEvent;

/**
 * Plugin reverse proxy router guard.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
public class HaloPluginReverseProxyGuard {

    private final ReverseProxyRouterRegistry reverseProxyRouterRegistry;

    public HaloPluginReverseProxyGuard(ReverseProxyRouterRegistry reverseProxyRouterRegistry) {
        this.reverseProxyRouterRegistry = reverseProxyRouterRegistry;
    }

    /**
     * Register reverse proxy route when the {@link HaloPluginStartedEvent} is triggered.
     *
     * @param event An instance of {@link HaloPluginStartedEvent}
     */
    @EventListener(HaloPluginStartedEvent.class)
    public void onPluginStarted(HaloPluginStartedEvent event) {
        log.debug("The plugin reverse proxy guard listens that the plugin is enabled and starts "
            + "registering reverse proxy routes.");
        PluginWrapper plugin = event.getPlugin();
        reverseProxyRouterRegistry.register(plugin);
    }

    /**
     * Destroy the plugin reverse proxy route when the plugin {@link HaloPluginStartedEvent} is
     * triggered.
     *
     * @param event An instance of {@link HaloPluginStoppedEvent}
     */
    @EventListener(HaloPluginStoppedEvent.class)
    public void onPluginStopped(HaloPluginStoppedEvent event) {
        log.debug(
            "The plugin reverse proxy guard listens that the plugin is disabled and starts to "
                + "destroy the reverse proxy route.");
        PluginWrapper plugin = event.getPlugin();
        reverseProxyRouterRegistry.remove(plugin);
    }
}
