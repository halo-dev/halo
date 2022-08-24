package run.halo.app.extension.gc;

import java.time.Duration;
import java.time.Instant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionConverter;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.DefaultController;
import run.halo.app.extension.controller.DefaultDelayQueue;
import run.halo.app.extension.store.ExtensionStoreClient;

@Configuration(proxyBeanMethods = false)
public class GarbageCollectorConfiguration {


    @Bean
    Controller garbageCollector(ExtensionClient client,
        ExtensionStoreClient storeClient,
        ExtensionConverter converter,
        SchemeManager schemeManager) {
        var reconciler = new GcReconciler(client, storeClient, converter);
        var queue = new DefaultDelayQueue<GcRequest>(Instant::now, Duration.ofMillis(500));
        var synchronizer = new GcSynchronizer(client, queue, schemeManager);
        return new DefaultController<>(
            "garbage-collector-controller",
            reconciler,
            queue,
            synchronizer,
            Duration.ofMillis(500),
            Duration.ofSeconds(1000)
        );
    }
}
