package run.halo.app.core.reconciler;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.index.query.Queries.equal;

import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import run.halo.app.content.comment.ReplyNotificationSubscriptionHelper;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.event.post.ReplyApprovedEvent;
import run.halo.app.event.post.ReplyChangedEvent;
import run.halo.app.event.post.ReplyCreatedEvent;
import run.halo.app.event.post.ReplyDeletedEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;

/**
 * Reconciler for {@link Reply}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class ReplyReconciler implements Reconciler<Reconciler.Request> {
    protected static final String FINALIZER_NAME = "reply-protection";

    /**
     * Annotation recording the notification state of a reply. A missing value is treated as already notified: replies
     * created before this annotation was introduced were notified on creation by the previous behavior, regardless of
     * their approval state.
     */
    static final String NOTIFICATION_ANNO = "content.halo.run/reply-notification";

    static final String NOTIFICATION_PENDING = "pending";
    static final String NOTIFICATION_SENT = "sent";

    private final ExtensionClient client;
    private final ApplicationEventPublisher eventPublisher;

    private final ReplyNotificationSubscriptionHelper replyNotificationSubscriptionHelper;

    @Override
    public Result reconcile(Request request) {
        client.fetch(Reply.class, request.name()).ifPresent(reply -> {
            if (reply.getMetadata().getDeletionTimestamp() != null) {
                cleanUpResourcesAndRemoveFinalizer(request.name());
                return;
            }
            var isNew = addFinalizers(reply.getMetadata(), Set.of(FINALIZER_NAME));
            if (isNew) {
                replyNotificationSubscriptionHelper.subscribeNewReplyReasonForReply(reply);
            }
            var notifyApproved = updateNotificationState(reply, isNew);
            if (isNew) {
                client.update(reply);
                eventPublisher.publishEvent(new ReplyCreatedEvent(this, reply));
            }

            if (reply.getSpec().getCreationTime() == null) {
                reply.getSpec()
                        .setCreationTime(defaultIfNull(
                                reply.getSpec().getApprovedTime(),
                                reply.getMetadata().getCreationTimestamp()));
            }

            // version + 1 is required to truly equal version
            // as a version will be incremented after the update
            reply.getStatus().setObservedVersion(reply.getMetadata().getVersion() + 1);

            client.update(reply);

            if (notifyApproved) {
                eventPublisher.publishEvent(new ReplyApprovedEvent(this, reply));
            }

            eventPublisher.publishEvent(new ReplyChangedEvent(this, reply));
        });
        return new Result(false, null);
    }

    private boolean updateNotificationState(Reply reply, boolean isNew) {
        var annotations = MetadataUtil.nullSafeAnnotations(reply);
        if (isNew) {
            var approved = Boolean.TRUE.equals(reply.getSpec().getApproved());
            annotations.put(NOTIFICATION_ANNO, approved ? NOTIFICATION_SENT : NOTIFICATION_PENDING);
            return false;
        }
        var notificationStatus = annotations.get(NOTIFICATION_ANNO);
        if (!NOTIFICATION_PENDING.equals(notificationStatus)
                || !Boolean.TRUE.equals(reply.getSpec().getApproved())) {
            return false;
        }
        annotations.put(NOTIFICATION_ANNO, NOTIFICATION_SENT);
        return true;
    }

    private void cleanUpResourcesAndRemoveFinalizer(String replyName) {
        client.fetch(Reply.class, replyName).ifPresent(reply -> {
            if (reply.getMetadata().getFinalizers() != null) {
                reply.getMetadata().getFinalizers().remove(FINALIZER_NAME);
            }
            client.update(reply);

            // on reply removing
            eventPublisher.publishEvent(new ReplyDeletedEvent(this, reply));
        });
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        var extension = new Reply();
        return builder.extension(extension)
                .syncAllListOptions(ListOptions.builder()
                        .andQuery(equal(Reply.REQUIRE_SYNC_ON_STARTUP_INDEX_NAME, true))
                        .build())
                .build();
    }
}
