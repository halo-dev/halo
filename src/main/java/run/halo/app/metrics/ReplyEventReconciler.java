package run.halo.app.metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.event.post.ReplyEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.DefaultController;
import run.halo.app.extension.controller.DefaultQueue;
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
public class ReplyEventReconciler implements Reconciler<ReplyEvent>, SmartLifecycle {
    private volatile boolean running = false;

    private final ExtensionClient client;
    private final RequestQueue<ReplyEvent> replyEventQueue;
    private final Controller replyEventController;

    public ReplyEventReconciler(ExtensionClient client) {
        this.client = client;
        replyEventQueue = new DefaultQueue<>(Instant::now);
        replyEventController = this.setupWith(null);
    }

    @Override
    public Result reconcile(ReplyEvent request) {
        Reply requestReply = request.getReply();
        String commentName = requestReply.getSpec().getCommentName();

        client.fetch(Comment.class, commentName)
            // if the comment has been deleted, then do nothing.
            .filter(comment -> comment.getMetadata().getDeletionTimestamp() == null)
            .ifPresent(comment -> {

                List<Reply> replies = client.list(Reply.class,
                    record -> commentName.equals(record.getSpec().getCommentName())
                        && record.getMetadata().getDeletionTimestamp() == null,
                    defaultReplyComparator());

                Comment.CommentStatus status = comment.getStatusOrDefault();
                // total reply count
                status.setReplyCount(replies.size());

                // calculate last reply time
                if (!replies.isEmpty()) {
                    Instant lastReplyTime = replies.get(0).getMetadata().getCreationTimestamp();
                    status.setLastReplyTime(lastReplyTime);
                }

                Instant lastReadTime = comment.getSpec().getLastReadTime();
                status.setUnreadReplyCount(Comment.getUnreadReplyCount(replies, lastReadTime));

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
            Duration.ofMillis(300),
            Duration.ofMinutes(5));
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

    @EventListener(ReplyEvent.class)
    public void onReplyAdded(ReplyEvent replyEvent) {
        replyEventQueue.addImmediately(replyEvent);
    }
}
