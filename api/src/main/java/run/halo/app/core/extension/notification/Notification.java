package run.halo.app.core.extension.notification;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * <p>{@link Notification} is a custom extension that used to store notification information for
 * inner use, it's on-site notification.</p>
 *
 * <p>Supports the following operations:</p>
 * <ul>
 *    <li>Marked as read: {@link NotificationSpec#setUnread(boolean)}</li>
 *    <li>Get the last read time: {@link NotificationSpec#getLastReadAt()}</li>
 *    <li>Filter by recipient: {@link NotificationSpec#getRecipient()}</li>
 * </ul>
 *
 * @author guqing
 * @see Reason
 * @see ReasonType
 * @since 2.10.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "notification.halo.run", version = "v1alpha1", kind = "Notification", plural =
    "notifications", singular = "notification")
public class Notification extends AbstractExtension {

    @Schema
    private NotificationSpec spec;

    @Data
    public static class NotificationSpec {
        @Schema(requiredMode = REQUIRED, minLength = 1, description = "The name of user")
        private String recipient;

        @Schema(requiredMode = REQUIRED, minLength = 1, description = "The name of reason")
        private String reason;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String title;

        @Schema(requiredMode = REQUIRED)
        private String rawContent;

        @Schema(requiredMode = REQUIRED)
        private String htmlContent;

        private boolean unread;

        private Instant lastReadAt;
    }
}
