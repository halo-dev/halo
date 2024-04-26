package run.halo.app.core.extension.reconciler;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.isDeleted;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;
import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.isNull;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.content.comment.ReplyNotificationSubscriptionHelper;
import run.halo.app.content.comment.ReplyService;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.core.extension.content.Constant;
import run.halo.app.event.post.CommentCreatedEvent;
import run.halo.app.event.post.CommentUnreadReplyCountChangedEvent;
import run.halo.app.extension.DefaultExtensionMatcher;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.Ref;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.index.query.Query;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.metrics.MeterUtils;

/**
 * Reconciler for {@link Comment}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
public class CommentReconciler implements Reconciler<Reconciler.Request> {
    public static final String FINALIZER_NAME = "comment-protection";
    private final ExtensionClient client;
    private final SchemeManager schemeManager;
    private final ReplyService replyService;
    private final ApplicationEventPublisher eventPublisher;

    private final ReplyNotificationSubscriptionHelper replyNotificationSubscriptionHelper;

    @Override
    public Result reconcile(Request request) {
        client.fetch(Comment.class, request.name())
            .ifPresent(comment -> {
                if (isDeleted(comment)) {
                    if (removeFinalizers(comment.getMetadata(), Set.of(FINALIZER_NAME))) {
                        cleanUpResources(comment);
                        client.update(comment);
                    }
                    return;
                }
                if (addFinalizers(comment.getMetadata(), Set.of(FINALIZER_NAME))) {
                    replyNotificationSubscriptionHelper.subscribeNewReplyReasonForComment(comment);
                    client.update(comment);
                    eventPublisher.publishEvent(new CommentCreatedEvent(this, comment));
                }

                compatibleCreationTime(comment);
                Comment.CommentStatus status = comment.getStatusOrDefault();
                status.setHasNewReply(defaultIfNull(status.getUnreadReplyCount(), 0) > 0);

                updateUnReplyCountIfNecessary(comment);
                updateSameSubjectRefCommentCounter(comment);

                // version + 1 is required to truly equal version
                // as a version will be incremented after the update
                comment.getStatusOrDefault()
                    .setObservedVersion(comment.getMetadata().getVersion() + 1);

                client.update(comment);
            });
        return new Result(false, null);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        var extension = new Comment();
        return builder
            .extension(extension)
            .onAddMatcher(DefaultExtensionMatcher.builder(client, extension.groupVersionKind())
                .fieldSelector(FieldSelector.of(
                    equal(Comment.REQUIRE_SYNC_ON_STARTUP_INDEX_NAME, BooleanUtils.TRUE))
                )
                .build()
            )
            .build();
    }

    /**
     * If the comment creation time is null, set it to the approved time or the current time.
     * TODO remove this method in the future and fill in attributes in hook mode instead.
     */
    void compatibleCreationTime(Comment comment) {
        var creationTime = comment.getSpec().getCreationTime();
        if (creationTime == null) {
            creationTime = defaultIfNull(comment.getSpec().getApprovedTime(),
                comment.getMetadata().getCreationTimestamp());
        }
        comment.getSpec().setCreationTime(creationTime);
    }

    private void updateUnReplyCountIfNecessary(Comment comment) {
        Instant lastReadTime = comment.getSpec().getLastReadTime();
        Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(comment);
        String lastReadTimeAnno = annotations.get(Constant.LAST_READ_TIME_ANNO);
        if (lastReadTime != null && lastReadTime.toString().equals(lastReadTimeAnno)) {
            return;
        }
        // delegate to other handler though event
        String commentName = comment.getMetadata().getName();
        eventPublisher.publishEvent(new CommentUnreadReplyCountChangedEvent(this, commentName));
        // handled flag
        if (lastReadTime != null) {
            annotations.put(Constant.LAST_READ_TIME_ANNO, lastReadTime.toString());
        } else {
            annotations.remove(Constant.LAST_READ_TIME_ANNO);
        }
    }

    private void updateSameSubjectRefCommentCounter(Comment comment) {
        var commentSubjectRef = comment.getSpec().getSubjectRef();
        GroupVersionKind groupVersionKind = groupVersionKind(commentSubjectRef);

        var totalCount = countTotalComments(commentSubjectRef);
        var approvedTotalCount = countApprovedComments(commentSubjectRef);
        schemeManager.fetch(groupVersionKind).ifPresent(scheme -> {
            String counterName = MeterUtils.nameOf(commentSubjectRef.getGroup(), scheme.plural(),
                commentSubjectRef.getName());
            client.fetch(Counter.class, counterName).ifPresentOrElse(counter -> {
                counter.setTotalComment(totalCount);
                counter.setApprovedComment(approvedTotalCount);
                client.update(counter);
            }, () -> {
                Counter counter = Counter.emptyCounter(counterName);
                counter.setTotalComment(totalCount);
                counter.setApprovedComment(approvedTotalCount);
                client.create(counter);
            });
        });
    }

    int countTotalComments(Ref commentSubjectRef) {
        var totalListOptions = new ListOptions();
        totalListOptions.setFieldSelector(FieldSelector.of(getBaseQuery(commentSubjectRef)));
        return (int) client.listBy(Comment.class, totalListOptions, PageRequestImpl.ofSize(1))
            .getTotal();
    }

    int countApprovedComments(Ref commentSubjectRef) {
        var approvedListOptions = new ListOptions();
        approvedListOptions.setFieldSelector(FieldSelector.of(and(
            getBaseQuery(commentSubjectRef),
            equal("spec.approved", BooleanUtils.TRUE)
        )));
        return (int) client.listBy(Comment.class, approvedListOptions, PageRequestImpl.ofSize(1))
            .getTotal();
    }

    private static Query getBaseQuery(Ref commentSubjectRef) {
        return and(equal("spec.subjectRef", Comment.toSubjectRefKey(commentSubjectRef)),
            isNull("metadata.deletionTimestamp"));
    }

    private void cleanUpResources(Comment comment) {
        // delete all replies under current comment
        replyService.removeAllByComment(comment.getMetadata().getName()).block();

        // decrement total comment count
        updateSameSubjectRefCommentCounter(comment);
    }

    @NonNull
    private GroupVersionKind groupVersionKind(@NonNull Ref ref) {
        return new GroupVersionKind(ref.getGroup(), ref.getVersion(), ref.getKind());
    }
}
