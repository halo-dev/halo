package run.halo.app.core.extension.notification;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * <p>{@link ReasonType} is a custom extension that defines a type of reason.</p>
 * <p>One {@link ReasonType} can have multiple {@link Reason}s to notify.</p>
 *
 * @author guqing
 * @see NotificationTemplate
 * @see Reason
 * @since 2.10.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "notification.halo.run", version = "v1alpha1", kind = "ReasonType",
    plural = "reasontypes", singular = "reasontype")
public class ReasonType extends AbstractExtension {
    public static final String LOCALIZED_RESOURCE_NAME_ANNO =
        "notification.halo.run/localized-resource-name";

    @Schema
    private Spec spec;

    @Data
    @Schema(name = "ReasonTypeSpec")
    public static class Spec {

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String displayName;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String description;

        private List<ReasonProperty> properties;
    }

    @Data
    public static class ReasonProperty {
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String name;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String type;

        private String description;

        @Schema(defaultValue = "false")
        private boolean optional;
    }
}
