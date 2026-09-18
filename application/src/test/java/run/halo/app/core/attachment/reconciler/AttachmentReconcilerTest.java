package run.halo.app.core.attachment.reconciler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.AttachmentChangedEvent;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Constant;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.extension.exception.ExtensionNotFoundException;

/** Tests for {@link AttachmentReconciler}. */
@ExtendWith(MockitoExtension.class)
class AttachmentReconcilerTest {

    @Mock
    private ExtensionClient client;

    @Mock
    private AttachmentService attachmentService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AttachmentReconciler reconciler;

    @Test
    void shouldRemoveFinalizerWhenPolicyWasDeleted() {
        var attachment = deletedAttachment();
        when(client.fetch(Attachment.class, "fake-attachment")).thenReturn(Optional.of(attachment));
        when(attachmentService.delete(attachment))
                .thenReturn(Mono.error(new ExtensionNotFoundException(
                        GroupVersionKind.fromExtension(Policy.class), "deleted-policy")));

        reconciler.reconcile(new Request("fake-attachment"));

        assertThat(attachment.getMetadata().getFinalizers()).doesNotContain(Constant.FINALIZER_NAME);
        verify(client).update(attachment);
        verify(eventPublisher).publishEvent(any(AttachmentChangedEvent.class));
    }

    @Test
    void shouldRemoveFinalizerAfterResourcesAreCleanedUp() {
        var attachment = deletedAttachment();
        when(client.fetch(Attachment.class, "fake-attachment")).thenReturn(Optional.of(attachment));
        when(attachmentService.delete(attachment)).thenReturn(Mono.just(attachment));

        reconciler.reconcile(new Request("fake-attachment"));

        assertThat(attachment.getMetadata().getFinalizers()).doesNotContain(Constant.FINALIZER_NAME);
        verify(client).update(attachment);
        verify(eventPublisher).publishEvent(any(AttachmentChangedEvent.class));
    }

    @Test
    void shouldKeepFinalizerWhenCleaningUpResourcesFailsForOtherReasons() {
        var attachment = deletedAttachment();
        when(client.fetch(Attachment.class, "fake-attachment")).thenReturn(Optional.of(attachment));
        when(attachmentService.delete(attachment)).thenReturn(Mono.error(new IllegalStateException("boom")));

        assertThatThrownBy(() -> reconciler.reconcile(new Request("fake-attachment")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");

        verify(client, never()).update(any(Attachment.class));
        verify(eventPublisher, never()).publishEvent(any(AttachmentChangedEvent.class));
    }

    private static Attachment deletedAttachment() {
        var attachment = new Attachment();
        var metadata = new Metadata();
        metadata.setName("fake-attachment");
        metadata.setDeletionTimestamp(Instant.now());
        metadata.setFinalizers(Set.of(Constant.FINALIZER_NAME));
        attachment.setMetadata(metadata);
        var spec = new Attachment.AttachmentSpec();
        spec.setPolicyName("deleted-policy");
        attachment.setSpec(spec);
        return attachment;
    }
}
