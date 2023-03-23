package run.halo.app.core.extension.reconciler;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import run.halo.app.content.comment.ReplyService;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Constant;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.Ref;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.metrics.MeterUtils;

/**
 * Reconciler for {@link Comment}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class CommentReconciler implements Reconciler<Reconciler.Request> {
    public static final String FINALIZER_NAME = "comment-protection";
    private final ExtensionClient client;
    private final SchemeManager schemeManager;

    public CommentReconciler(ExtensionClient client, SchemeManager schemeManager) {
        this.client = client;
        this.schemeManager = schemeManager;
    }

    @Override
    public Result reconcile(Request request) {
        client.fetch(Comment.class, request.name())
            .ifPresent(comment -> {
                if (isDeleted(comment)) {
                    cleanUpResourcesAndRemoveFinalizer(request.name());
                    return;
                }
                addFinalizerIfNecessary(comment);
                compatibleCreationTime(request.name());
                reconcileStatus(request.name());
                updateSameSubjectRefCommentCounter(comment.getSpec().getSubjectRef());
            });
        return new Result(false, null);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Comment())
            .build();
    }

    /**
     * If the comment creation time is null, set it to the approved time or the current time.
     * TODO remove this method in the future and fill in attributes in hook mode instead.
     *
     * @param name comment name
     */
    void compatibleCreationTime(String name) {
        client.fetch(Comment.class, name).ifPresent(comment -> {
            Instant creationTime = comment.getSpec().getCreationTime();
            Instant oldCreationTime =
                creationTime == null ? null : Instant.ofEpochMilli(creationTime.toEpochMilli());
            if (creationTime == null) {
                creationTime = defaultIfNull(comment.getSpec().getApprovedTime(), Instant.now());
                comment.getSpec().setCreationTime(creationTime);
            }

            if (!Objects.equals(oldCreationTime, comment.getSpec().getCreationTime())) {
                client.update(comment);
            }
        });
    }

    private boolean isDeleted(Comment comment) {
        return comment.getMetadata().getDeletionTimestamp() != null;
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

    private void reconcileStatus(String name) {
        client.fetch(Comment.class, name).ifPresent(comment -> {
            Comment oldComment = JsonUtils.deepCopy(comment);
            Comment.CommentStatus status = comment.getStatusOrDefault();
            status.setHasNewReply(defaultIfNull(status.getUnreadReplyCount(), 0) > 0);
            updateUnReplyCountIfNecessary(comment);
            if (!oldComment.equals(comment)) {
                client.update(comment);
            }
        });
    }

    private void updateUnReplyCountIfNecessary(Comment comment) {
        Instant lastReadTime = comment.getSpec().getLastReadTime();
        Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(comment);
        String lastReadTimeAnno = annotations.get(Constant.LAST_READ_TIME_ANNO);
        if (lastReadTime != null && lastReadTime.toString().equals(lastReadTimeAnno)) {
            return;
        }
        // spec.lastReadTime is null or not equal to annotation.lastReadTime
        String commentName = comment.getMetadata().getName();
        List<Reply> replies = client.list(Reply.class,
            reply -> commentName.equals(reply.getSpec().getCommentName())
                && reply.getMetadata().getDeletionTimestamp() == null,
            ReplyService.creationTimeAscComparator());

        // calculate unread reply count
        comment.getStatusOrDefault()
            .setUnreadReplyCount(Comment.getUnreadReplyCount(replies, lastReadTime));
        // handled flag
        if (lastReadTime != null) {
            annotations.put(Constant.LAST_READ_TIME_ANNO, lastReadTime.toString());
        }
    }

    private void updateSameSubjectRefCommentCounter(Ref commentSubjectRef) {
        List<Comment> comments = client.list(Comment.class,
            comment -> !isDeleted(comment)
                && commentSubjectRef.equals(comment.getSpec().getSubjectRef()),
            null);

        GroupVersionKind groupVersionKind = groupVersionKind(commentSubjectRef);
        if (groupVersionKind == null) {
            return;
        }
        // approved total count
        long approvedTotalCount = comments.stream()
            .filter(comment -> BooleanUtils.isTrue(comment.getSpec().getApproved()))
            .count();
        // total count
        int totalCount = comments.size();

        schemeManager.fetch(groupVersionKind).ifPresent(scheme -> {
            String counterName = MeterUtils.nameOf(commentSubjectRef.getGroup(), scheme.plural(),
                commentSubjectRef.getName());
            client.fetch(Counter.class, counterName).ifPresentOrElse(counter -> {
                counter.setTotalComment(totalCount);
                counter.setApprovedComment((int) approvedTotalCount);
                client.update(counter);
            }, () -> {
                Counter counter = Counter.emptyCounter(counterName);
                counter.setTotalComment(totalCount);
                counter.setApprovedComment((int) approvedTotalCount);
                client.create(counter);
            });
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
        // decrement total comment count
        updateSameSubjectRefCommentCounter(comment.getSpec().getSubjectRef());
    }

    @Nullable
    private GroupVersionKind groupVersionKind(Ref ref) {
        if (ref == null) {
            return null;
        }
        return new GroupVersionKind(ref.getGroup(), ref.getVersion(), ref.getKind());
    }
}
