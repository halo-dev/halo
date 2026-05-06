package run.halo.app.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.notification.Notification;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ReactiveExtensionPaginatedOperator;

/**
 * Tests for {@link NotificationAutoCleanupTask}.
 *
 * @author programmerloverun
 * @since 2.22.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationAutoCleanupTaskTest {

    @Mock
    private ReactiveExtensionPaginatedOperator paginatedOperator;

    @Mock
    private ReactiveExtensionClient client;

    @Mock
    private NotificationAutoCleanupProperties properties;

    @InjectMocks
    private NotificationAutoCleanupTask task;

    // -----------------------------------------------------------------------
    // Guard: disabled
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("cleanUp() skips everything when auto-cleanup is disabled")
    void cleanUp_skips_whenDisabled() {
        when(properties.isEnabled()).thenReturn(false);

        task.cleanUp();

        verify(paginatedOperator, never()).deleteInitialBatch(any(), any());
        verify(client, never()).listAll(any(), any(), any());
    }

    // -----------------------------------------------------------------------
    // Strategy 1: retention by days
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Retention-by-days strategy")
    class RetentionByDaysTests {

        @BeforeEach
        void setup() {
            when(properties.isEnabled()).thenReturn(true);
            when(properties.isRetentionByDaysEnabled()).thenReturn(true);
            when(properties.isRetentionByCountEnabled()).thenReturn(false);
            when(properties.getRetentionDays()).thenReturn(30);
        }

        @Test
        @DisplayName("getRetentionThreshold() returns approximately now minus retentionDays")
        void retentionThreshold_isCorrect() {
            Instant lower = Instant.now().minus(Duration.ofDays(30)).minusSeconds(1);
            Instant threshold = task.getRetentionThreshold();
            Instant upper = Instant.now().minus(Duration.ofDays(30)).plusSeconds(1);

            assertThat(threshold).isBetween(lower, upper);
        }

        @Test
        @DisplayName("cleanUpByRetentionDays() returns count of deleted notifications")
        void cleanUpByRetentionDays_returnsDeletedCount() {
            Notification n1 = buildNotification("n1", "alice");
            Notification n2 = buildNotification("n2", "bob");
            when(paginatedOperator.deleteInitialBatch(eq(Notification.class), any()))
                .thenReturn(Flux.just(n1, n2));

            long deleted = task.cleanUpByRetentionDays();

            assertThat(deleted).isEqualTo(2);
            verify(paginatedOperator, times(1))
                .deleteInitialBatch(eq(Notification.class), any(ListOptions.class));
        }

        @Test
        @DisplayName("cleanUpByRetentionDays() returns 0 when no expired notifications")
        void cleanUpByRetentionDays_returnsZero_whenNothingExpired() {
            when(paginatedOperator.deleteInitialBatch(eq(Notification.class), any()))
                .thenReturn(Flux.empty());

            long deleted = task.cleanUpByRetentionDays();

            assertThat(deleted).isZero();
        }
    }

    // -----------------------------------------------------------------------
    // Strategy 2: retention by count per user
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Retention-by-count strategy")
    class RetentionByCountTests {

        @BeforeEach
        void setup() {
            when(properties.isEnabled()).thenReturn(true);
            when(properties.isRetentionByDaysEnabled()).thenReturn(false);
            when(properties.isRetentionByCountEnabled()).thenReturn(true);
            when(properties.getMaxCountPerUser()).thenReturn(2);
        }

        @Test
        @DisplayName("Does NOT delete when user notification count is within limit")
        void noDelete_whenCountWithinLimit() {
            when(client.listAll(eq(User.class), any(), any()))
                .thenReturn(Flux.just(buildUser("alice")));
            // alice has exactly 2 — at the limit, nothing to delete
            when(client.listBy(eq(Notification.class), any(), any()))
                .thenReturn(Mono.just(new ListResult<>(0, 1, 2L, List.of())));

            long deleted = task.cleanUpByMaxCountPerUser();

            assertThat(deleted).isZero();
            verify(paginatedOperator, never()).list(any(), any());
        }

        @Test
        @DisplayName("Deletes excess notifications when user count exceeds limit")
        void deletesExcess_whenCountExceedsLimit() {
            when(client.listAll(eq(User.class), any(), any()))
                .thenReturn(Flux.just(buildUser("alice")));

            // alice has 5, limit is 2 → 3 should be deleted
            Notification n1 = buildNotification("n1", "alice");
            Notification n2 = buildNotification("n2", "alice");
            Notification n3 = buildNotification("n3", "alice");

            when(client.listBy(eq(Notification.class), any(), any()))
                .thenReturn(Mono.just(new ListResult<>(0, 1, 5L, List.of())));
            when(paginatedOperator.list(eq(Notification.class), any()))
                .thenReturn(Flux.just(n1, n2, n3));
            when(client.delete(n1)).thenReturn(Mono.just(n1));
            when(client.delete(n2)).thenReturn(Mono.just(n2));
            when(client.delete(n3)).thenReturn(Mono.just(n3));

            long deleted = task.cleanUpByMaxCountPerUser();

            assertThat(deleted).isEqualTo(3);
        }

        @Test
        @DisplayName("Handles multiple users independently")
        void handlesMultipleUsers() {
            when(client.listAll(eq(User.class), any(), any()))
                .thenReturn(Flux.just(buildUser("alice"), buildUser("bob")));

            // alice: 3 notifications, limit 2 → delete 1
            Notification aliceOld = buildNotification("a-old", "alice");
            // bob: 1 notification, within limit → delete 0
            when(client.listBy(eq(Notification.class), any(), any()))
                .thenReturn(
                    Mono.just(new ListResult<>(0, 1, 3L, List.of())),  // alice
                    Mono.just(new ListResult<>(0, 1, 1L, List.of()))   // bob
                );
            when(paginatedOperator.list(eq(Notification.class), any()))
                .thenReturn(Flux.just(aliceOld));
            when(client.delete(aliceOld)).thenReturn(Mono.just(aliceOld));

            long deleted = task.cleanUpByMaxCountPerUser();

            assertThat(deleted).isEqualTo(1);
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static Notification buildNotification(String name, String recipient) {
        var notification = new Notification();
        var metadata = new Metadata();
        metadata.setName(name);
        notification.setMetadata(metadata);
        var spec = new Notification.NotificationSpec();
        spec.setRecipient(recipient);
        spec.setUnread(true);
        notification.setSpec(spec);
        return notification;
    }

    private static User buildUser(String name) {
        var user = new User();
        var metadata = new Metadata();
        metadata.setName(name);
        user.setMetadata(metadata);
        return user;
    }
}
