package run.halo.app.core.extension.notification;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * <p>{@link NotifierDescriptor} is a custom extension that defines a notifier.</p>
 * <p>It describes the notifier's name, description, and the extension name of the notifier to
 * let the user know what the notifier is and what it can do in the UI and also let the
 * {@code NotificationCenter} know how to load the notifier and prepare the notifier's settings.</p>
 *
 * @author guqing
 * @since 2.10.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "notification.halo.run", version = "v1alpha1", kind = "NotifierDescriptor",
    plural = "notifierDescriptors", singular = "notifierDescriptor")
public class NotifierDescriptor extends AbstractExtension {

    @Schema
    private Spec spec;

    @Data
    @Schema(name = "NotifierDescriptorSpec")
    public static class Spec {
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String displayName;

        private String description;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String notifierExtName;

        private SettingRef senderSettingRef;

        private SettingRef receiverSettingRef;
    }

    @Data
    @Schema(name = "NotifierSettingRef")
    public static class SettingRef {
        @Schema(requiredMode = REQUIRED)
        private String name;

        @Schema(requiredMode = REQUIRED)
        private String group;
    }
}
