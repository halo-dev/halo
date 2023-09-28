package run.halo.app.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;

/**
 * Test for {@link NotificationTrigger}.
 *
 * @author guqing
 * @since 2.9.0
 */
@ExtendWith(MockitoExtension.class)
class NotificationTriggerTest {

    @Mock
    private ExtensionClient client;

    @Mock
    private NotificationCenter notificationCenter;

    @InjectMocks
    private NotificationTrigger notificationTrigger;

    @Test
    void reconcile() {
        var reason = mock(Reason.class);
        var metadata = mock(Metadata.class);
        when(reason.getMetadata()).thenReturn(metadata);
        when(metadata.getDeletionTimestamp()).thenReturn(null);
        when(metadata.getFinalizers()).thenReturn(Set.of());

        when(client.fetch(eq(Reason.class), eq("fake-reason")))
            .thenReturn(Optional.of(reason));

        when(notificationCenter.notify(eq(reason))).thenReturn(Mono.empty());
        notificationTrigger.reconcile(new Reconciler.Request("fake-reason"));

        verify(notificationCenter).notify(eq(reason));
        verify(metadata).setFinalizers(eq(Set.of(NotificationTrigger.TRIGGERED_FINALIZER)));
        verify(client).update(any(Reason.class));
    }
}