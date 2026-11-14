package run.halo.app.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.extension.index.query.Queries.equal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.notification.Notification;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ReactiveExtensionPaginatedOperator;
import run.halo.app.infra.exception.AccessDeniedException;

/** Tests for {@link DefaultNotificationService}. */
@ExtendWith(MockitoExtension.class)
class DefaultNotificationServiceTest {

    @Mock
    private ReactiveExtensionClient client;

    @Mock
    private ReactiveExtensionPaginatedOperator paginatedOperator;

    @InjectMocks
    private DefaultNotificationService service;

    @Test
    void shouldGetNotificationByRecipient() {
        var notification = notification("n1", "alice", true);
        when(client.get(Notification.class, "n1")).thenReturn(Mono.just(notification));

        service.getByName("alice", "n1")
                .as(StepVerifier::create)
                .expectNext(notification)
                .verifyComplete();
    }

    @Test
    void shouldRejectNotificationOfAnotherRecipient() {
        var notification = notification("n1", "alice", true);
        when(client.get(Notification.class, "n1")).thenReturn(Mono.just(notification));

        service.getByName("bob", "n1").as(StepVerifier::create).verifyError(AccessDeniedException.class);
    }

    @Test
    void shouldMarkOnlyUnreadNotificationsAsRead() {
        var unread = notification("n1", "alice", true);
        var read = notification("n2", "alice", false);
        when(paginatedOperator.list(eq(Notification.class), any(ListOptions.class)))
                .thenReturn(Flux.just(unread, read));
        when(client.fetch(Notification.class, "n1")).thenReturn(Mono.just(unread));
        when(client.update(unread)).thenReturn(Mono.just(unread));

        service.markAllAsRead("alice").as(StepVerifier::create).expectNext("n1").verifyComplete();

        assertThat(unread.getSpec().isUnread()).isFalse();
        verify(client, never()).fetch(Notification.class, "n2");
    }

    @Test
    void shouldDeleteAllNotificationsMatchingQuery() {
        var listOptions =
                ListOptions.builder().andQuery(equal("spec.recipient", "alice")).build();
        var query = mock(UserNotificationQuery.class);
        when(query.toListOptions()).thenReturn(listOptions);
        when(paginatedOperator.deleteInitialBatch(eq(Notification.class), any(ListOptions.class)))
                .thenReturn(Flux.<Notification>empty());

        service.deleteAllByUser(query).as(StepVerifier::create).verifyComplete();

        verify(paginatedOperator).deleteInitialBatch(Notification.class, listOptions);
    }

    private static Notification notification(String name, String recipient, boolean unread) {
        var metadata = new Metadata();
        metadata.setName(name);

        var spec = new Notification.NotificationSpec();
        spec.setRecipient(recipient);
        spec.setUnread(unread);

        var notification = new Notification();
        notification.setMetadata(metadata);
        notification.setSpec(spec);
        return notification;
    }
}
