package run.halo.app.metrics;

import java.time.Duration;
import java.time.Instant;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.content.Post;
import run.halo.app.event.post.CounterEvent;
import run.halo.app.event.post.CounterUpdatedEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.DefaultController;
import run.halo.app.extension.controller.DefaultQueue;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequestQueue;
import run.halo.app.extension.index.Indexer;
import run.halo.app.extension.index.IndexerFactoryImpl;
import run.halo.app.infra.utils.JsonUtils;


@Component
public class PostCounterEventReconciler implements Reconciler<CounterEvent>, SmartLifecycle {
    private final RequestQueue<CounterEvent> counterEventQueue;

    private final ExtensionClient client;

    private final IndexerFactoryImpl indexerFactory;

    private volatile boolean running = false;
    private final Controller counterEventController;

    public PostCounterEventReconciler(ExtensionClient client, IndexerFactoryImpl indexerFactory) {
        this.counterEventQueue = new DefaultQueue<>(Instant::now);
        this.client = client;
        this.indexerFactory = indexerFactory;
        this.counterEventController = this.setupWith(null);
    }


    @Override
    public Result reconcile(CounterEvent request) {
        if (request instanceof CounterUpdatedEvent) {
            updateIndexByPost(request.getCounter(),
                getPostName(request.getCounter().getMetadata().getName()));
        }
        return Result.doNotRetry();
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return new DefaultController<>(
            this.getClass().getName(),
            this,
            counterEventQueue,
            null,
            Duration.ofMillis(300),
            Duration.ofMinutes(5));
    }

    private void updateIndexByPost(Counter counter, String postName) {
        client.fetch(Post.class, postName)
            .ifPresent(post -> {
                post.getMetadata().getAnnotations()
                    .put(Post.COUNTER_ANNO, JsonUtils.objectToJson(counter));
                GroupVersionKind gvk = GroupVersionKind.fromExtension(Post.class);
                Indexer indexer = indexerFactory.getIndexer(gvk);
                indexer.updateRecord(post);
            });
    }

    String getPostName(String name) {
        if (name.contains("/")) {
            return name.split("/")[1];
        }
        return name;
    }

    @EventListener(CounterEvent.class)
    public void onCounterEvent(CounterEvent counterEvent) {
        counterEventQueue.addImmediately(counterEvent);
    }

    @Override
    public void start() {
        this.counterEventController.start();
        this.running = true;
    }

    @Override
    public void stop() {
        this.running = false;
        this.counterEventController.dispose();
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }
}
