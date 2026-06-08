package run.halo.app.core.extension.notification;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * Notifier descriptor extension that advertises a notification delivery implementation and its settings.
 *
 * <p>It describes the notifier's name, description, and the extension name of the notifier to let the user know what
 * the notifier is and what it can do in the UI and also let the {@code NotificationCenter} know how to load the
 * notifier and prepare the notifier's settings.
 *
 * @author guqing
 * @since 2.10.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(
        group = "notification.halo.run",
        version = "v1alpha1",
        kind = "NotifierDescriptor",
        plural = "notifierDescriptors",
        singular = "notifierDescriptor")
public class NotifierDescriptor extends AbstractExtension {

    /** Desired notifier display metadata and setting references. */
    @Schema
    private Spec spec;

    /** Desired notifier descriptor metadata and configuration references. */
    @Data
    @Schema(name = "NotifierDescriptorSpec")
    public static class Spec {
        /** Display name shown for the notifier. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String displayName;

        /** Human-readable description of what the notifier sends. */
        private String description;

        /** Extension name of the notifier implementation. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String notifierExtName;

        /** Setting reference used to configure the sender side of the notifier. */
        private SettingRef senderSettingRef;

        /** Setting reference used to configure the receiver side of the notifier. */
        private SettingRef receiverSettingRef;
    }

    /** Reference to a notifier setting form group. */
    @Data
    @Schema(name = "NotifierSettingRef")
    public static class SettingRef {
        /** Setting metadata.name. */
        @Schema(requiredMode = REQUIRED)
        private String name;

        /** Setting form group name. */
        @Schema(requiredMode = REQUIRED)
        private String group;
    }
}
