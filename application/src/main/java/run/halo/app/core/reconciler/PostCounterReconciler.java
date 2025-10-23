package run.halo.app.core.reconciler;

import static run.halo.app.extension.index.query.Queries.startsWith;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import run.halo.app.core.counter.MeterUtils;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.content.Post;
import run.halo.app.event.post.PostStatsChangedEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;

@Component
@RequiredArgsConstructor
public class PostCounterReconciler implements Reconciler<Reconciler.Request> {

    private final ApplicationEventPublisher eventPublisher;
    private final ExtensionClient client;

    @Override
    public Result reconcile(Request request) {
        if (!isSameAsPost(request.name())) {
            return Result.doNotRetry();
        }
        client.fetch(Counter.class, request.name()).ifPresent(counter -> {
            eventPublisher.publishEvent(new PostStatsChangedEvent(this, counter));
        });
        return Result.doNotRetry();
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        var extension = new Counter();
        return builder
            .extension(extension)
            .syncAllListOptions(ListOptions.builder()
                .andQuery(startsWith("metadata.name", MeterUtils.nameOf(Post.class, "")))
                .build())
            .build();
    }

    static boolean isSameAsPost(String name) {
        return name.startsWith(MeterUtils.nameOf(Post.class, ""));
    }
}
