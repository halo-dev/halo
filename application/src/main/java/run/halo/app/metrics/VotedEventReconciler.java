package run.halo.app.metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Counter;
import run.halo.app.event.post.DownvotedEvent;
import run.halo.app.event.post.UpvotedEvent;
import run.halo.app.event.post.VotedEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.DefaultController;
import run.halo.app.extension.controller.DefaultQueue;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequestQueue;

/**
 * Update counters after receiving upvote or downvote event.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
public class VotedEventReconciler implements Reconciler<VotedEvent>, SmartLifecycle {
    private volatile boolean running = false;

    private final ExtensionClient client;
    private final RequestQueue<VotedEvent> votedEventQueue;
    private final Controller votedEventController;

    public VotedEventReconciler(ExtensionClient client) {
        this.client = client;
        votedEventQueue = new DefaultQueue<>(Instant::now);
        votedEventController = this.setupWith(null);
    }

    @Override
    public Result reconcile(VotedEvent votedEvent) {
        String counterName =
            MeterUtils.nameOf(votedEvent.getGroup(), votedEvent.getPlural(), votedEvent.getName());
        client.fetch(Counter.class, counterName)
            .ifPresentOrElse(counter -> {
                if (votedEvent instanceof UpvotedEvent) {
                    Integer existingVote = ObjectUtils.defaultIfNull(counter.getUpvote(), 0);
                    counter.setUpvote(existingVote + 1);
                } else if (votedEvent instanceof DownvotedEvent) {
                    Integer existingVote = ObjectUtils.defaultIfNull(counter.getDownvote(), 0);
                    counter.setDownvote(existingVote + 1);
                }
                client.update(counter);
            }, () -> {
                Counter counter = Counter.emptyCounter(counterName);
                if (votedEvent instanceof UpvotedEvent) {
                    counter.setUpvote(1);
                } else if (votedEvent instanceof DownvotedEvent) {
                    counter.setDownvote(1);
                }
                client.create(counter);
            });
        return new Result(false, null);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return new DefaultController<>(
            this.getClass().getName(),
            this,
            votedEventQueue,
            null,
            Duration.ofMillis(300),
            Duration.ofMinutes(5));
    }

    @Override
    public void start() {
        this.votedEventController.start();
        this.running = true;
    }

    @Override
    public void stop() {
        this.running = false;
        this.votedEventController.dispose();
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Component
    @RequiredArgsConstructor
    public class VotedEventListener {
        private final SchemeManager schemeManager;

        /**
         * Add up/down vote event to queue.
         */
        @Async
        @EventListener(VotedEvent.class)
        public void onVoted(VotedEvent event) {
            var gpn = new GroupPluralName(event.getGroup(), event.getPlural(), event.getName());
            if (!checkSubject(gpn)) {
                log.debug("Skip voted event for: {}", gpn);
                return;
            }
            votedEventQueue.addImmediately(event);
        }

        private boolean checkSubject(
            GroupPluralName groupPluralName) {
            Optional<Scheme> schemeOptional = schemeManager.schemes().stream()
                .filter(scheme -> {
                    GroupVersionKind gvk = scheme.groupVersionKind();
                    return scheme.plural().equals(groupPluralName.plural())
                        && gvk.group().equals(groupPluralName.group());
                })
                .findFirst();
            return schemeOptional.map(
                    scheme -> client.fetch(scheme.groupVersionKind(), groupPluralName.name())
                        .isPresent()
                )
                .orElse(false);
        }

        record GroupPluralName(String group, String plural, String name) {
        }
    }
}
