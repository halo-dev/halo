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
import run.halo.app.core.extension.notification.Notification;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.ReactiveExtensionPaginatedOperator;
import run.halo.app.infra.properties.HaloProperties;

/**
 * Tests for {@link NotificationAutoCleanupTask}.
 *
 * @author programmerloverun
 * @since 2.25.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationAutoCleanupTaskTest {

    @Mock
    private ReactiveExtensionPaginatedOperator paginatedOperator;

    @Mock
    private HaloProperties haloProperties;

    @Mock
    private NotificationProperties notificationProperties;

    @InjectMocks
    private NotificationAutoCleanupTask task;

    @BeforeEach
    void setupHaloProperties() {
        when(haloProperties.getNotification()).thenReturn(notificationProperties);
    }

    // -----------------------------------------------------------------------
    // Guard: disabled
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("cleanUp() skips everything when auto-cleanup is disabled")
    void cleanUp_skips_whenDisabled() {
        when(notificationProperties.isEnabled()).thenReturn(false);

        task.cleanUp();

        verify(paginatedOperator, never()).deleteInitialBatch(any(), any());
    }

    // -----------------------------------------------------------------------
    // Strategy: retention by days
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Retention-by-days strategy")
    class RetentionByDaysTests {

        @BeforeEach
        void setup() {
            when(notificationProperties.isEnabled()).thenReturn(true);
            when(notificationProperties.getRetentionDays()).thenReturn(30);
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
            Notification n1 = buildNotification("n1", false);
            Notification n2 = buildNotification("n2", false);
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

        @Test
        @DisplayName("buildExpiredListOptions() includes spec.unread=false condition")
        void buildExpiredListOptions_includesUnreadFalse() {
            Instant threshold = Instant.now().minus(Duration.ofDays(30));
            ListOptions opts = task.buildExpiredListOptions(threshold);

            String selector = opts.getFieldSelector().query().toString();
            assertThat(selector).contains("spec.unread");
            assertThat(selector).contains("false");
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static Notification buildNotification(String name, boolean unread) {
        var notification = new Notification();
        var metadata = new Metadata();
        metadata.setName(name);
        notification.setMetadata(metadata);
        var spec = new Notification.NotificationSpec();
        spec.setUnread(unread);
        notification.setSpec(spec);
        return notification;
    }
}
