package run.halo.app.core.extension.reconciler;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.content.Post;
import run.halo.app.event.post.PostChangeEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.index.Indexer;
import run.halo.app.extension.index.IndexerFactoryImpl;
import run.halo.app.metrics.MeterUtils;


@Component
public class PostCounterReconciler implements Reconciler<Reconciler.Request> {

    private final ExtensionClient client;

    private final IndexerFactoryImpl indexerFactory;

    private final Set<String> set;

    public PostCounterReconciler(ExtensionClient client, IndexerFactoryImpl indexerFactory) {
        this.client = client;
        this.indexerFactory = indexerFactory;
        this.set = new CopyOnWriteArraySet<>();
    }

    @Override
    public Result reconcile(Request request) {
        String name = request.name();
        client.fetch(Counter.class, name).ifPresent(counter -> {
            set.add(getPostName(name));
        });
        return Result.doNotRetry();
    }


    @Scheduled(cron = "0/3 * * * * ?")
    public void handleSetRequest() {
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            String name = iterator.next();
            updateCounter(name);
            set.remove(name);
        }
    }

    private void updateCounter(String postName) {

        client.fetch(Post.class, postName)
            .ifPresent(post -> {
                client.fetch(Counter.class,
                        MeterUtils.nameOf(Post.class, post.getMetadata().getName()))
                    .ifPresent(queryCounter -> {
                        post.getMetadata().getAnnotations()
                            .put(Post.COUNTER_VISIT_ANNO, queryCounter.getVisit().toString());
                        post.getMetadata().getAnnotations()
                            .put(Post.COUNTER_COMMENT_ANNO,
                                queryCounter.getTotalComment().toString());
                        updateIndexByPost(post);
                    });
            });
    }

    public void updateIndexByPost(Post post) {
        GroupVersionKind gvk = GroupVersionKind.fromExtension(Post.class);
        Indexer indexer = indexerFactory.getIndexer(gvk);
        indexer.updateRecord(post);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Counter())
            .syncAllOnStart(false).build();
    }

    @EventListener(ApplicationStartedEvent.class)
    public void preBuildCounterIndexes() {
        client.listAll(Counter.class, new ListOptions(), Sort.unsorted()).forEach(counter -> {
            client.fetch(Post.class, getPostName(counter.getMetadata().getName()))
                .ifPresent(post -> {
                    post.getMetadata().getAnnotations()
                        .put(Post.COUNTER_VISIT_ANNO, counter.getVisit().toString());
                    post.getMetadata().getAnnotations()
                        .put(Post.COUNTER_COMMENT_ANNO, counter.getTotalComment().toString());
                    updateIndexByPost(post);
                });
        });
    }

    @EventListener(PostChangeEvent.class)
    public void onPostChanged(PostChangeEvent event) {
        set.add(event.getName());
    }

    public String getPostName(String counterName) {
        return counterName.split("/")[1];
    }
}
