package run.halo.app.core.extension.reconciler;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import run.halo.app.core.extension.Comment;
import run.halo.app.core.extension.Reply;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Reconciler for {@link Comment}.
 *
 * @author guqing
 * @since 2.0.0
 */
public class CommentReconciler implements Reconciler<Reconciler.Request> {
    private static final String FINALIZER_NAME = "comment-protection";
    private final ExtensionClient client;

    public CommentReconciler(ExtensionClient client) {
        this.client = client;
    }

    @Override
    public Result reconcile(Request request) {
        return client.fetch(Comment.class, request.name())
            .map(comment -> {
                if (isDeleted(comment)) {
                    addFinalizerIfNecessary(comment);
                }
                reconcileStatus(request.name());
                return new Result(true, Duration.ofMinutes(1));
            })
            .orElseGet(() -> new Result(false, null));
    }

    private boolean isDeleted(Comment comment) {
        return comment.getMetadata().getDeletionTimestamp() != null;
    }

    private void reconcileStatus(String name) {
        client.fetch(Comment.class, name).ifPresent(comment -> {
            final Comment oldComment = JsonUtils.deepCopy(comment);

            List<Reply> replies = client.list(Reply.class,
                reply -> name.equals(reply.getSpec().getCommentName()),
                defaultReplyComparator());
            // calculate reply count
            comment.getStatusOrDefault().setReplyCount(replies.size());
            // calculate last reply time
            if (!replies.isEmpty()) {
                Instant lastReplyTime = replies.get(0).getMetadata().getCreationTimestamp();
                comment.getStatusOrDefault().setLastReplyTime(lastReplyTime);
            }
            // calculate unread reply count
            Instant lastReadTime = comment.getSpec().getLastReadTime();
            long unreadReplyCount = replies.stream()
                .filter(reply -> {
                    if (lastReadTime == null) {
                        return true;
                    }
                    return reply.getMetadata().getCreationTimestamp().isAfter(lastReadTime);
                })
                .count();
            comment.getStatusOrDefault().setUnreadReplyCount((int) unreadReplyCount);

            if (!oldComment.equals(comment)) {
                client.update(comment);
            }
        });
    }

    private void addFinalizerIfNecessary(Comment oldComment) {
        Set<String> finalizers = oldComment.getMetadata().getFinalizers();
        if (finalizers != null && finalizers.contains(FINALIZER_NAME)) {
            return;
        }
        client.fetch(Comment.class, oldComment.getMetadata().getName())
            .ifPresent(comment -> {
                Set<String> newFinalizers = comment.getMetadata().getFinalizers();
                if (newFinalizers == null) {
                    newFinalizers = new HashSet<>();
                    comment.getMetadata().setFinalizers(newFinalizers);
                }
                newFinalizers.add(FINALIZER_NAME);
                client.update(comment);
            });
    }

    private void cleanUpResourcesAndRemoveFinalizer(String commentName) {
        client.fetch(Comment.class, commentName).ifPresent(comment -> {
            cleanUpResources(comment);
            if (comment.getMetadata().getFinalizers() != null) {
                comment.getMetadata().getFinalizers().remove(FINALIZER_NAME);
            }
            client.update(comment);
        });
    }

    private void cleanUpResources(Comment comment) {
        // delete all replies under current comment
        client.list(Reply.class, reply -> comment.getMetadata().getName()
                    .equals(reply.getSpec().getCommentName()),
                null)
            .forEach(client::delete);
    }

    Comparator<Reply> defaultReplyComparator() {
        Function<Reply, Instant> createTime = reply -> reply.getMetadata().getCreationTimestamp();
        return Comparator.comparing(createTime)
            .thenComparing(reply -> reply.getMetadata().getName())
            .reversed();
    }
}
