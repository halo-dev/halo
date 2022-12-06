package run.halo.app.core.extension.reconciler;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Ref;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
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
                updateCommentCounter();
            });
        return new Result(false, null);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Comment())
            .build();
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

    private void updateCommentCounter() {
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
        });
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
        updateCommentCounter();
    }

    @Nullable
    private GroupVersionKind groupVersionKind(Ref ref) {
        if (ref == null) {
            return null;
        }
        return new GroupVersionKind(ref.getGroup(), ref.getVersion(), ref.getKind());
    }
}
