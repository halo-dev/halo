package run.halo.app.plugin;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.retry.RetryException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.event.HaloPluginBeforeStopEvent;

/**
 * Synchronization listener executed by the plugin before it is stopped.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
public class PluginBeforeStopSyncListener {

    private final ReactiveExtensionClient client;

    public PluginBeforeStopSyncListener(ReactiveExtensionClient client) {
        this.client = client;
    }

    @EventListener
    public void onApplicationEvent(@NonNull HaloPluginBeforeStopEvent event) {
        var pluginWrapper = event.getPlugin();
        var p = pluginWrapper.getPlugin();
        if (!(p instanceof SpringPlugin springPlugin)) {
            return;
        }
        var applicationContext = springPlugin.getApplicationContext();
        if (!(applicationContext instanceof PluginApplicationContext pluginApplicationContext)) {
            return;
        }
        cleanUpPluginExtensionResources(pluginApplicationContext).block(Duration.ofMinutes(1));
    }

    private Mono<Void> cleanUpPluginExtensionResources(PluginApplicationContext context) {
        var gvkExtensionNames = context.extensionNamesMapping();
        return Flux.fromIterable(gvkExtensionNames.entrySet())
            .flatMap(entry -> Flux.fromIterable(entry.getValue())
                .flatMap(extensionName -> client.fetch(entry.getKey(), extensionName))
                .flatMap(client::delete)
                .flatMap(e -> waitForDeleted(e.groupVersionKind(), e.getMetadata().getName())))
            .then();
    }

    private Mono<Void> waitForDeleted(GroupVersionKind gvk, String name) {
        return client.fetch(gvk, name)
            .flatMap(e -> {
                if (log.isDebugEnabled()) {
                    log.debug("Wait for {}/{} deleted", gvk, name);
                }
                return Mono.error(new RetryException("Wait for extension deleted"));
            })
            .retryWhen(Retry.backoff(10, Duration.ofMillis(100))
                .filter(RetryException.class::isInstance))
            .then()
            .doOnSuccess(v -> {
                if (log.isDebugEnabled()) {
                    log.debug("{}/{} was deleted successfully.", gvk, name);
                }
            });
    }
}
