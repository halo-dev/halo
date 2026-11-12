package run.halo.app.core.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import run.halo.app.content.comment.ReplyNotificationSubscriptionHelper;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.event.post.ReplyApprovedEvent;
import run.halo.app.event.post.ReplyCreatedEvent;
import run.halo.app.event.post.ReplyDeletedEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;

/**
 * Tests for {@link ReplyReconciler}.
 *
 * @since 2.28.0
 */
@ExtendWith(MockitoExtension.class)
class ReplyReconcilerTest {

    @Mock
    private ExtensionClient client;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ReplyNotificationSubscriptionHelper replyNotificationSubscriptionHelper;

    @InjectMocks
    private ReplyReconciler replyReconciler;

    @Test
    void shouldPublishCreatedEventForNewApprovedReply() {
        var reply = createReply(true, null);
        when(client.fetch(eq(Reply.class), eq("fake-reply"))).thenReturn(Optional.of(reply));

        replyReconciler.reconcile(new Reconciler.Request("fake-reply"));

        assertThat(reply.getMetadata().getFinalizers()).contains(ReplyReconciler.FINALIZER_NAME);
        assertThat(reply.getMetadata().getAnnotations())
                .containsEntry(ReplyReconciler.NOTIFICATION_ANNO, ReplyReconciler.NOTIFICATION_SENT);
        verify(replyNotificationSubscriptionHelper).subscribeNewReplyReasonForReply(reply);
        verify(eventPublisher).publishEvent(isA(ReplyCreatedEvent.class));
        verify(eventPublisher, never()).publishEvent(isA(ReplyApprovedEvent.class));
    }

    @Test
    void shouldMarkPendingInsteadOfPublishingForNewUnapprovedReply() {
        var reply = createReply(false, null);
        when(client.fetch(eq(Reply.class), eq("fake-reply"))).thenReturn(Optional.of(reply));

        replyReconciler.reconcile(new Reconciler.Request("fake-reply"));

        assertThat(reply.getMetadata().getAnnotations())
                .containsEntry(ReplyReconciler.NOTIFICATION_ANNO, ReplyReconciler.NOTIFICATION_PENDING);
        verify(eventPublisher, never()).publishEvent(isA(ReplyApprovedEvent.class));
    }

    @Test
    void shouldPublishApprovedEventWhenPendingReplyGetsApproved() {
        var reply = createReply(true, ReplyReconciler.NOTIFICATION_PENDING);
        reply.getMetadata().setFinalizers(new HashSet<>(Set.of(ReplyReconciler.FINALIZER_NAME)));
        when(client.fetch(eq(Reply.class), eq("fake-reply"))).thenReturn(Optional.of(reply));
        var markerAtUpdate = new AtomicReference<String>();
        doAnswer(invocation -> {
                    Reply updated = invocation.getArgument(0);
                    markerAtUpdate.set(updated.getMetadata().getAnnotations().get(ReplyReconciler.NOTIFICATION_ANNO));
                    return null;
                })
                .when(client)
                .update(reply);

        replyReconciler.reconcile(new Reconciler.Request("fake-reply"));

        assertThat(reply.getMetadata().getAnnotations())
                .containsEntry(ReplyReconciler.NOTIFICATION_ANNO, ReplyReconciler.NOTIFICATION_SENT);
        assertThat(markerAtUpdate).hasValue(ReplyReconciler.NOTIFICATION_SENT);
        var inOrder = inOrder(client, eventPublisher);
        inOrder.verify(client).update(reply);
        inOrder.verify(eventPublisher).publishEvent(isA(ReplyApprovedEvent.class));
        verify(replyNotificationSubscriptionHelper, never()).subscribeNewReplyReasonForReply(any());
    }

    @Test
    void shouldNotPublishAgainForAlreadyNotifiedReply() {
        var reply = createReply(true, ReplyReconciler.NOTIFICATION_SENT);
        reply.getMetadata().setFinalizers(new HashSet<>(Set.of(ReplyReconciler.FINALIZER_NAME)));
        when(client.fetch(eq(Reply.class), eq("fake-reply"))).thenReturn(Optional.of(reply));

        replyReconciler.reconcile(new Reconciler.Request("fake-reply"));

        verify(eventPublisher, never()).publishEvent(isA(ReplyApprovedEvent.class));
    }

    @Test
    void shouldNotNotifyLegacyApprovedReplyWithoutMarker() {
        var reply = createReply(true, null);
        reply.getMetadata().setFinalizers(new HashSet<>(Set.of(ReplyReconciler.FINALIZER_NAME)));
        when(client.fetch(eq(Reply.class), eq("fake-reply"))).thenReturn(Optional.of(reply));

        replyReconciler.reconcile(new Reconciler.Request("fake-reply"));

        assertThat(reply.getMetadata().getAnnotations()).doesNotContainKey(ReplyReconciler.NOTIFICATION_ANNO);
        verify(eventPublisher, never()).publishEvent(isA(ReplyApprovedEvent.class));
    }

    @Test
    void shouldNotNotifyWhenDeletingUnapprovedReply() {
        var reply = createReply(false, ReplyReconciler.NOTIFICATION_PENDING);
        reply.getMetadata().setDeletionTimestamp(Instant.now());
        reply.getMetadata().setFinalizers(new HashSet<>(Set.of(ReplyReconciler.FINALIZER_NAME)));
        when(client.fetch(eq(Reply.class), eq("fake-reply"))).thenReturn(Optional.of(reply));

        replyReconciler.reconcile(new Reconciler.Request("fake-reply"));

        assertThat(reply.getMetadata().getFinalizers()).isEmpty();
        verify(eventPublisher).publishEvent(isA(ReplyDeletedEvent.class));
        verify(eventPublisher, never()).publishEvent(isA(ReplyApprovedEvent.class));
    }

    @Test
    void shouldNotInitializeLegacyReplyAsPending() {
        var reply = createReply(false, null);
        reply.getMetadata().setFinalizers(new HashSet<>(Set.of(ReplyReconciler.FINALIZER_NAME)));
        when(client.fetch(eq(Reply.class), eq("fake-reply"))).thenReturn(Optional.of(reply));

        replyReconciler.reconcile(new Reconciler.Request("fake-reply"));

        assertThat(reply.getMetadata().getAnnotations()).doesNotContainKey(ReplyReconciler.NOTIFICATION_ANNO);
        verify(eventPublisher, never()).publishEvent(isA(ReplyApprovedEvent.class));
    }

    @Test
    void shouldNotNotifyLegacyReplyAfterUnapprovalAndReapproval() {
        var legacyApproved = createReply(true, null);
        legacyApproved.getMetadata().setFinalizers(new HashSet<>(Set.of(ReplyReconciler.FINALIZER_NAME)));
        when(client.fetch(eq(Reply.class), eq("fake-reply"))).thenReturn(Optional.of(legacyApproved));
        replyReconciler.reconcile(new Reconciler.Request("fake-reply"));

        var unapproved = createReply(false, null);
        unapproved.getMetadata().setFinalizers(new HashSet<>(Set.of(ReplyReconciler.FINALIZER_NAME)));
        when(client.fetch(eq(Reply.class), eq("fake-reply"))).thenReturn(Optional.of(unapproved));
        replyReconciler.reconcile(new Reconciler.Request("fake-reply"));

        var reapproved = createReply(true, null);
        reapproved.getMetadata().setFinalizers(new HashSet<>(Set.of(ReplyReconciler.FINALIZER_NAME)));
        when(client.fetch(eq(Reply.class), eq("fake-reply"))).thenReturn(Optional.of(reapproved));
        replyReconciler.reconcile(new Reconciler.Request("fake-reply"));

        assertThat(legacyApproved.getMetadata().getAnnotations()).doesNotContainKey(ReplyReconciler.NOTIFICATION_ANNO);
        assertThat(unapproved.getMetadata().getAnnotations()).doesNotContainKey(ReplyReconciler.NOTIFICATION_ANNO);
        assertThat(reapproved.getMetadata().getAnnotations()).doesNotContainKey(ReplyReconciler.NOTIFICATION_ANNO);
        verify(eventPublisher, never()).publishEvent(isA(ReplyApprovedEvent.class));
    }

    private Reply createReply(boolean approved, String notificationStatus) {
        var metadata = new Metadata();
        metadata.setName("fake-reply");
        metadata.setVersion(1L);
        Map<String, String> annotations = new HashMap<>();
        if (notificationStatus != null) {
            annotations.put(ReplyReconciler.NOTIFICATION_ANNO, notificationStatus);
        }
        metadata.setAnnotations(annotations);

        var spec = new Reply.ReplySpec();
        spec.setCommentName("fake-comment");
        spec.setContent("fake content");
        spec.setApproved(approved);

        var reply = new Reply();
        reply.setMetadata(metadata);
        reply.setSpec(spec);
        return reply;
    }
}
