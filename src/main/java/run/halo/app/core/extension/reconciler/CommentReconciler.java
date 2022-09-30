package run.halo.app.core.extension.reconciler;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.lang.Nullable;
import run.halo.app.core.extension.Comment;
import run.halo.app.core.extension.Reply;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Ref;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.metrics.MeterUtils;

/**
 * Reconciler for {@link Comment}.
 *
 * @author guqing
 * @since 2.0.0
 */
public class CommentReconciler implements Reconciler<Reconciler.Request> {
    public static final String FINALIZER_NAME = "comment-protection";
    private final ExtensionClient client;
    private final MeterRegistry meterRegistry;
    private final SchemeManager schemeManager;

    public CommentReconciler(ExtensionClient client, MeterRegistry meterRegistry,
        SchemeManager schemeManager) {
        this.client = client;
        this.meterRegistry = meterRegistry;
        this.schemeManager = schemeManager;
    }

    @Override
    public Result reconcile(Request request) {
        return client.fetch(Comment.class, request.name())
            .map(comment -> {
                if (isDeleted(comment)) {
                    cleanUpResourcesAndRemoveFinalizer(request.name());
                    return new Result(false, null);
                }
                addFinalizerIfNecessary(comment);
                reconcileStatus(request.name());
                reconcileCommentCount();
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

    private void reconcileCommentCount() {
        Map<Ref, List<RefCommentTuple>> map = client.list(Comment.class, null, null)
            .stream()
            .map(comment -> {
                boolean approved =
                    Objects.equals(true, comment.getSpec().getApproved())
                        && !isDeleted(comment);
                return new RefCommentTuple(comment.getSpec().getSubjectRef(),
                    comment.getMetadata().getName(), approved);
            })
            .collect(Collectors.groupingBy(RefCommentTuple::ref));
        map.forEach((ref, pairs) -> {
            GroupVersionKind groupVersionKind = groupVersionKind(ref);
            if (groupVersionKind == null) {
                return;
            }
            // approved total count
            long approvedTotalCount = pairs.stream()
                .filter(refCommentPair -> refCommentPair.approved)
                .count();
            // total count
            int totalCount = pairs.size();

            schemeManager.fetch(groupVersionKind).ifPresent(scheme -> {
                String counterName = MeterUtils.nameOf(ref.getGroup(), scheme.plural(),
                    ref.getName());
                // meter for total comment count
                calcTotalComments(totalCount, counterName);
                // meter for approved comment count
                calcApprovedComments(approvedTotalCount, counterName);
            });
        });
    }

    private void calcTotalComments(int totalCount, String counterName) {
        Counter totalCommentCounter =
            MeterUtils.totalCommentCounter(meterRegistry, counterName);
        double totalCountMeter = totalCommentCounter.count();
        double totalIncrement = totalCount - totalCountMeter;
        if (totalCountMeter + totalIncrement >= 0) {
            totalCommentCounter.increment(totalIncrement);
        } else {
            totalCommentCounter.increment(totalCountMeter * -1);
        }
    }

    private void calcApprovedComments(long approvedTotalCount, String counterName) {
        Counter approvedCommentCounter =
            MeterUtils.approvedCommentCounter(meterRegistry, counterName);
        double approvedComments = approvedCommentCounter.count();
        double increment = approvedTotalCount - approvedCommentCounter.count();
        if (approvedComments + increment >= 0) {
            approvedCommentCounter.increment(increment);
        } else {
            approvedCommentCounter.increment(approvedComments * -1);
        }
    }

    record RefCommentTuple(Ref ref, String name, boolean approved) {
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
        Ref subjectRef = comment.getSpec().getSubjectRef();
        GroupVersionKind groupVersionKind = groupVersionKind(subjectRef);
        if (groupVersionKind == null) {
            return;
        }
        schemeManager.fetch(groupVersionKind)
            .ifPresent(scheme -> {
                String counterName = MeterUtils.nameOf(subjectRef.getGroup(), scheme.plural(),
                    subjectRef.getName());
                MeterUtils.totalCommentCounter(meterRegistry, counterName)
                    .increment(-1);
            });
    }

    @Nullable
    private GroupVersionKind groupVersionKind(Ref ref) {
        if (ref == null) {
            return null;
        }
        return new GroupVersionKind(ref.getGroup(), ref.getVersion(), ref.getKind());
    }

    Comparator<Reply> defaultReplyComparator() {
        Function<Reply, Instant> createTime = reply -> reply.getMetadata().getCreationTimestamp();
        return Comparator.comparing(createTime)
            .thenComparing(reply -> reply.getMetadata().getName())
            .reversed();
    }
}
