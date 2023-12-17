package run.halo.app.plugin;

import io.micrometer.common.util.StringUtils;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.DefaultController;
import run.halo.app.extension.controller.DefaultQueue;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequestQueue;
import run.halo.app.plugin.event.PluginCreatedEvent;

/**
 * Plugin event reconciler.
 * If other plugin events need to be reconciled, consider sharing this reconciler.
 *
 * @author guqing
 * @since 2.2.0
 */
@Slf4j
@Component
public class PluginCreatedEventReconciler
    implements Reconciler<PluginCreatedEvent>, SmartLifecycle {

    private final RequestQueue<PluginCreatedEvent> pluginEventQueue;

    private final ReactiveExtensionClient client;

    private final Controller pluginEventController;

    private boolean running = false;

    public PluginCreatedEventReconciler(ReactiveExtensionClient client) {
        this.client = client;
        pluginEventQueue = new DefaultQueue<>(Instant::now);
        pluginEventController = this.setupWith(null);
    }

    @Override
    public Result reconcile(PluginCreatedEvent pluginCreatedEvent) {
        String pluginName = pluginCreatedEvent.getPluginName();
        try {
            ensureConfigMapNameNotEmptyIfSettingIsNotBlank(pluginName);
        } catch (InterruptedException e) {
            throw Exceptions.propagate(e);
        }
        return null;
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return new DefaultController<>(
            this.getClass().getName(),
            this,
            pluginEventQueue,
            null,
            Duration.ofMillis(100),
            Duration.ofSeconds(1000)
        );
    }

    @EventListener(PluginCreatedEvent.class)
    public void handlePluginCreated(PluginCreatedEvent pluginCreatedEvent) {
        pluginEventQueue.addImmediately(pluginCreatedEvent);
    }

    void ensureConfigMapNameNotEmptyIfSettingIsNotBlank(String pluginName)
        throws InterruptedException {
        client.fetch(Plugin.class, pluginName)
            .switchIfEmpty(Mono.error(new IllegalStateException("Plugin not found: " + pluginName)))
            .filter(plugin -> StringUtils.isNotBlank(plugin.getSpec().getSettingName()))
            .filter(plugin -> StringUtils.isBlank(plugin.getSpec().getConfigMapName()))
            .doOnNext(plugin -> {
                // has settingName value but configMapName not configured
                plugin.getSpec().setConfigMapName(UUID.randomUUID().toString());
            })
            .flatMap(client::update)
            .block();
    }

    @Override
    public void start() {
        pluginEventController.start();
        running = true;
    }

    @Override
    public void stop() {
        running = false;
        pluginEventController.dispose();
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
