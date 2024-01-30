package run.halo.app.core.extension.notification;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * <p>{@link NotificationTemplate} is a custom extension that defines a notification template.</p>
 * <p>It describes the notification template's name, description, and the template content.</p>
 * <p>{@link Spec#getReasonSelector()} is used to select the template by reasonType and language,
 * if multiple templates are matched, the best match will be selected. This is useful when you
 * want to override the default template.</p>
 *
 * @author guqing
 * @since 2.10.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "notification.halo.run", version = "v1alpha1", kind = "NotificationTemplate",
    plural = "notificationtemplates", singular = "notificationtemplate")
public class NotificationTemplate extends AbstractExtension {

    @Schema
    private Spec spec;

    @Data
    @Schema(name = "NotificationTemplateSpec")
    public static class Spec {
        @Schema
        private ReasonSelector reasonSelector;

        @Schema
        private Template template;
    }

    @Data
    @Schema(name = "TemplateContent")
    public static class Template {
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String title;

        private String htmlBody;

        private String rawBody;
    }

    @Data
    public static class ReasonSelector {
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String reasonType;

        @Schema(requiredMode = REQUIRED, minLength = 1, defaultValue = "default")
        private String language;
    }
}
