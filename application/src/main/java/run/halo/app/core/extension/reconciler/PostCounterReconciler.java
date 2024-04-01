package run.halo.app.core.extension.reconciler;

import java.time.Instant;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.content.Post;
import run.halo.app.event.post.CounterUpdatedEvent;
import run.halo.app.event.post.PostChangeEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.DefaultQueue;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequestQueue;
import run.halo.app.metrics.MeterUtils;


@Component
public class PostCounterReconciler implements Reconciler<Reconciler.Request> {

    private final ExtensionClient client;
    private final RequestQueue<Request> queue;
    private final ApplicationEventPublisher eventPublisher;


    public PostCounterReconciler(ExtensionClient client,
        ApplicationEventPublisher eventPublisher) {
        this.client = client;
        this.eventPublisher = eventPublisher;
        this.queue = new DefaultQueue<>(Instant::now);
    }

    @Override
    public Result reconcile(Request request) {
        String name = request.name();
        if (isPostCounter(name)) {
            client.fetch(Counter.class, getCounterName(name)).ifPresent(counter -> {
                eventPublisher.publishEvent(new CounterUpdatedEvent(this, counter));
            });
        }
        return Result.doNotRetry();
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
