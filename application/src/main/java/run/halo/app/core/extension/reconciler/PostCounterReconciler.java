package run.halo.app.core.extension.reconciler;

import java.time.Instant;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.content.Post;
import run.halo.app.event.post.PostChangeEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.DefaultQueue;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequestQueue;
import run.halo.app.extension.index.Indexer;
import run.halo.app.extension.index.IndexerFactoryImpl;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.metrics.MeterUtils;


@Component
public class PostCounterReconciler implements Reconciler<Reconciler.Request> {

    private final ExtensionClient client;

    private final IndexerFactoryImpl indexerFactory;

    private final RequestQueue<Request> queue;


    public PostCounterReconciler(ExtensionClient client, IndexerFactoryImpl indexerFactory) {
        this.client = client;
        this.indexerFactory = indexerFactory;
        this.queue = new DefaultQueue<>(Instant::now);
    }

    @Override
    public Result reconcile(Request request) {
        String name = request.name();
        if (isPostCounter(name)) {
            client.fetch(Counter.class, getCounterName(name)).ifPresent(counter -> {
                updateIndexByPost(counter, getPostName(name));
            });
        }
        return Result.doNotRetry();
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

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Counter())
            .syncAllOnStart(true)
            .queue(queue)
            .build();
    }

    @EventListener(PostChangeEvent.class)
    public void onPostChanged(PostChangeEvent event) {
        queue.addImmediately(new Request(event.getName()));
    }

    String getPostName(String name) {
        if (name.contains("/")) {
            return name.split("/")[1];
        }
        return name;
    }

    String getCounterName(String name) {
        if (name.contains("/")) {
            return name;
        }
        return MeterUtils.nameOf(Post.class, name);
    }

    boolean isPostCounter(String counterName) {
        if (counterName.contains("/")) {
            return MeterUtils.nameOf(Post.class, getPostName(counterName)).equals(counterName);
        }
        return true;
    }
}
