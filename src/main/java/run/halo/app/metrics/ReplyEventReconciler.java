package run.halo.app.metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.event.post.ReplyCreatedEvent;
import run.halo.app.event.post.ReplyDeletedEvent;
import run.halo.app.event.post.ReplyEvent;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Watcher;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.DefaultController;
import run.halo.app.extension.controller.DefaultDelayQueue;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequestQueue;

/**
 * Update the comment status after receiving the reply event.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
public class ReplyEventReconciler implements Reconciler<ReplyEvent>, SmartLifecycle, Watcher {
    private volatile boolean running = false;

    private final ExtensionClient client;
    private final ApplicationEventPublisher eventPublisher;
    private final RequestQueue<ReplyEvent> replyEventQueue;
    private final Controller replyEventController;

    public ReplyEventReconciler(ExtensionClient client, ApplicationEventPublisher eventPublisher) {
        this.client = client;
        this.eventPublisher = eventPublisher;
        replyEventQueue = new DefaultDelayQueue<>(Instant::now);
        replyEventController = this.setupWith(null);
    }

    @Override
    public Result reconcile(ReplyEvent request) {
        Reply reply = request.getReply();
        String commentName = reply.getSpec().getCommentName();

        List<Reply> replies = client.list(Reply.class,
            record -> commentName.equals(reply.getSpec().getCommentName()),
            defaultReplyComparator());

        client.fetch(Comment.class, reply.getSpec().getCommentName())
            .ifPresent(comment -> {
                Comment.CommentStatus status = comment.getStatusOrDefault();
                // total reply count
                status.setReplyCount(replies.size());

                // calculate last reply time
                if (!replies.isEmpty()) {
                    Instant lastReplyTime = replies.get(0).getMetadata().getCreationTimestamp();
                    status.setLastReplyTime(lastReplyTime);
                }

                // calculate unread reply count
                Instant lastReadTime = comment.getSpec().getLastReadTime();
                long unreadReplyCount = replies.stream()
                    .filter(existingReply -> {
                        if (lastReadTime == null) {
                            return true;
                        }
                        return existingReply.getMetadata().getCreationTimestamp()
                            .isAfter(lastReadTime);
                    })
                    .count();
                status.setUnreadReplyCount((int) unreadReplyCount);
                client.update(comment);
            });
        return new Result(false, null);
    }

    Comparator<Reply> defaultReplyComparator() {
        Function<Reply, Instant> createTime = reply -> reply.getMetadata().getCreationTimestamp();
        return Comparator.comparing(createTime)
            .thenComparing(reply -> reply.getMetadata().getName())
            .reversed();
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return new DefaultController<>(
            this.getClass().getName(),
            this,
            replyEventQueue,
            null,
            Duration.ofMillis(100),
            Duration.ofMinutes(10));
    }

    @Override
    public void start() {
        this.replyEventController.start();
        this.running = true;
    }

    @Override
    public void stop() {
        this.running = false;
        this.replyEventController.dispose();
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean isDisposed() {
        return !isRunning();
    }

    @Override
    public void onAdd(Extension extension) {
        ReplyCreatedEvent replyCreatedEvent = new ReplyCreatedEvent(this, (Reply) extension);
        eventPublisher.publishEvent(replyCreatedEvent);
        replyEventQueue.addImmediately(replyCreatedEvent);
    }

    @Override
    public void onDelete(Extension extension) {
        ReplyDeletedEvent replyDeletedEvent = new ReplyDeletedEvent(this, (Reply) extension);
        eventPublisher.publishEvent(replyDeletedEvent);
        replyEventQueue.addImmediately(replyDeletedEvent);
    }
}
