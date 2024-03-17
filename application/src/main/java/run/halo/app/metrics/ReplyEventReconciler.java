package run.halo.app.metrics;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.greaterThan;
import static run.halo.app.extension.index.query.QueryFactory.isNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.event.post.CommentUnreadReplyCountChangedEvent;
import run.halo.app.event.post.ReplyEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.DefaultController;
import run.halo.app.extension.controller.DefaultQueue;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequestQueue;
import run.halo.app.extension.index.query.Query;
import run.halo.app.extension.router.selector.FieldSelector;

/**
 * Update the comment status after receiving the reply event.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
public class ReplyEventReconciler
    implements Reconciler<ReplyEventReconciler.CommentName>, SmartLifecycle {
    private volatile boolean running = false;

    private final ExtensionClient client;
    private final RequestQueue<CommentName> replyEventQueue;
    private final Controller replyEventController;

    public ReplyEventReconciler(ExtensionClient client) {
        this.client = client;
        replyEventQueue = new DefaultQueue<>(Instant::now);
        replyEventController = this.setupWith(null);
    }

    @Override
    public Result reconcile(CommentName request) {
        String commentName = request.name();

        client.fetch(Comment.class, commentName)
            // if the comment has been deleted, then do nothing.
            .filter(comment -> comment.getMetadata().getDeletionTimestamp() == null)
            .ifPresent(comment -> {
                // order by reply creation time desc to get first as last reply time
                var baseQuery = and(
                    equal("spec.commentName", commentName),
                    isNull("metadata.deletionTimestamp")
                );
                var pageRequest = PageRequestImpl.ofSize(1).withSort(
                    Sort.by("spec.creationTime", "metadata.name").descending()
                );
                final Comment.CommentStatus status = comment.getStatusOrDefault();

                var replyPageResult =
                    client.listBy(Reply.class, listOptionsWithFieldQuery(baseQuery), pageRequest);
                // total reply count
                status.setReplyCount((int) replyPageResult.getTotal());

                // calculate last reply time from total replies(top 1)
                Instant lastReplyTime = replyPageResult.get()
                    .map(reply -> reply.getSpec().getCreationTime())
                    .findFirst()
                    .orElse(null);
                status.setLastReplyTime(lastReplyTime);

                // calculate visible reply count(only approved and not hidden)
                var visibleReplyPageResult =
                    client.listBy(Reply.class, listOptionsWithFieldQuery(and(
                        baseQuery,
                        equal("spec.approved", BooleanUtils.TRUE),
                        equal("spec.hidden", BooleanUtils.FALSE)
                    )), pageRequest);
                status.setVisibleReplyCount((int) visibleReplyPageResult.getTotal());

                // calculate unread reply count(after last read time)
                var unReadQuery = Optional.ofNullable(comment.getSpec().getLastReadTime())
                    .map(lastReadTime -> and(
                        baseQuery,
                        greaterThan("spec.creationTime", lastReadTime.toString())
                    ))
                    .orElse(baseQuery);
                var unReadPageResult =
                    client.listBy(Reply.class, listOptionsWithFieldQuery(unReadQuery), pageRequest);
                status.setUnreadReplyCount((int) unReadPageResult.getTotal());

                status.setHasNewReply(defaultIfNull(status.getUnreadReplyCount(), 0) > 0);

                client.update(comment);
            });
        return new Result(false, null);
    }

    public record CommentName(String name) {
        public static CommentName of(String name) {
            return new CommentName(name);
        }
    }

    static ListOptions listOptionsWithFieldQuery(Query query) {
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(query));
        return listOptions;
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
    public void onReplyEvent(ReplyEvent replyEvent) {
        var commentName = replyEvent.getReply().getSpec().getCommentName();
        replyEventQueue.addImmediately(CommentName.of(commentName));
    }

    @EventListener(CommentUnreadReplyCountChangedEvent.class)
    public void onUnreadReplyCountChangedEvent(CommentUnreadReplyCountChangedEvent event) {
        replyEventQueue.addImmediately(CommentName.of(event.getCommentName()));
    }
}
