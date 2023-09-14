package run.halo.app.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DefaultNotificationSender}.
 *
 * @author guqing
 * @since 2.9.0
 */
class DefaultNotificationSenderTest {

    @Nested
    class QueueItemTest {

        @Test
        void equalsTest() {
            var item1 =
                new DefaultNotificationSender.QueueItem("1",
                    mock(DefaultNotificationSender.SendNotificationTask.class), 0);
            var item2 =
                new DefaultNotificationSender.QueueItem("1",
                    mock(DefaultNotificationSender.SendNotificationTask.class), 1);

            assertThat(item1).isEqualTo(item2);
        }
    }
}