package run.halo.app.core.extension.notification;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * Notification extension that stores an on-site notification delivered to a Halo user.
 *
 * <p>Supports the following operations:
 *
 * <ul>
 *   <li>Marked as read: {@link NotificationSpec#setUnread(boolean)}
 *   <li>Get the last read time: {@link NotificationSpec#getLastReadAt()}
 *   <li>Filter by recipient: {@link NotificationSpec#getRecipient()}
 * </ul>
 *
 * @author guqing
 * @see Reason
 * @see ReasonType
 * @since 2.10.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(
        group = "notification.halo.run",
        version = "v1alpha1",
        kind = "Notification",
        plural = "notifications",
        singular = "notification")
public class Notification extends AbstractExtension {

    /** Desired notification recipient, content, and read state. */
    @Schema
    private NotificationSpec spec;

    /** Notification content and read state for one recipient. */
    @Data
    public static class NotificationSpec {
        /** User metadata.name that receives the notification. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String recipient;

        /** Reason metadata.name that generated this notification. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String reason;

        /** Notification title shown in the notification center. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String title;

        /** Plain text or source notification content. */
        @Schema(requiredMode = REQUIRED)
        private String rawContent;

        /** HTML notification content. */
        @Schema(requiredMode = REQUIRED)
        private String htmlContent;

        /** Whether the recipient has not read the notification yet. */
        private boolean unread;

        /** Last time the recipient marked this notification as read. */
        private Instant lastReadAt;
    }
}
