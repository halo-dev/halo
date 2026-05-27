package run.halo.app.core.extension.notification;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * Notification template extension that defines localized message content for matching reason types.
 *
 * <p>It describes the notification template's name, description, and the template content.
 *
 * <p>{@link Spec#getReasonSelector()} is used to select the template by reasonType and language, if multiple templates
 * are matched, the best match will be selected. This is useful when you want to override the default template.
 *
 * @author guqing
 * @since 2.10.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(
        group = "notification.halo.run",
        version = "v1alpha1",
        kind = "NotificationTemplate",
        plural = "notificationtemplates",
        singular = "notificationtemplate")
public class NotificationTemplate extends AbstractExtension {

    /** Desired selector and template content. */
    @Schema
    private Spec spec;

    /** Desired notification template selector and content. */
    @Data
    @Schema(name = "NotificationTemplateSpec")
    public static class Spec {
        /** Selector that determines which reasons can use this template. */
        @Schema
        private ReasonSelector reasonSelector;

        /** Title and body content rendered for matching reasons. */
        @Schema
        private Template template;
    }

    /** Notification template content. */
    @Data
    @Schema(name = "TemplateContent")
    public static class Template {
        /** Rendered notification title. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String title;

        /** HTML body rendered for notification channels that support HTML. */
        private String htmlBody;

        /** Plain text or source body rendered for notification channels that do not use HTML. */
        private String rawBody;
    }

    /** Selector used to match a notification template to a reason type and language. */
    @Data
    public static class ReasonSelector {
        /** ReasonType metadata.name this template applies to. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String reasonType;

        /** Language tag this template applies to, or default for the fallback template. */
        @Schema(requiredMode = REQUIRED, minLength = 1, defaultValue = "default")
        private String language;
    }
}
