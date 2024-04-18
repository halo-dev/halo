package run.halo.app.metrics;

import java.time.Duration;
import java.time.Instant;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import run.halo.app.content.Stats;
import run.halo.app.core.extension.content.Post;
import run.halo.app.event.post.PostStatsChangedEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.DefaultController;
import run.halo.app.extension.controller.DefaultQueue;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequestQueue;
import run.halo.app.infra.utils.JsonUtils;

@Component
public class PostStatsUpdater implements Reconciler<PostStatsUpdater.StatsRequest>,
    SmartLifecycle {

    private volatile boolean running = false;

    private final ExtensionClient client;
    private final RequestQueue<StatsRequest> queue;
    private final Controller controller;

    public PostStatsUpdater(ExtensionClient client) {
        this.client = client;
        queue = new DefaultQueue<>(Instant::now);
        controller = this.setupWith(null);
    }

    @Override
    public Result reconcile(StatsRequest request) {
        client.fetch(Post.class, request.postName()).ifPresent(post -> {
            var annotations = MetadataUtil.nullSafeAnnotations(post);
            annotations.put(Post.STATS_ANNO, JsonUtils.objectToJson(request.stats()));
            client.update(post);
        });
        return Result.doNotRetry();
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return new DefaultController<>(
            this.getClass().getName(),
            this,
            queue,
            null,
            Duration.ofMillis(100),
            Duration.ofMinutes(10));
    }

    @Override
    public void start() {
        this.controller.start();
        this.running = true;
    }

    @Override
    public void stop() {
        this.running = false;
        this.controller.dispose();
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @EventListener(PostStatsChangedEvent.class)
    public void onReplyEvent(PostStatsChangedEvent event) {
        var counter = event.getCounter();
        var stats = Stats.builder()
            .visit(counter.getVisit())
            .upvote(counter.getUpvote())
            .totalComment(counter.getTotalComment())
            .approvedComment(counter.getApprovedComment())
            .build();
        var request = new StatsRequest(event.getPostName(), stats);
        queue.addImmediately(request);
    }

    public record StatsRequest(String postName, Stats stats) {
    }
}
