package run.halo.app.core.extension.notification;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * Reason type extension that defines the schema and display metadata for notification reasons.
 *
 * <p>One {@link ReasonType} can have multiple {@link Reason}s to notify.
 *
 * @author guqing
 * @see NotificationTemplate
 * @see Reason
 * @since 2.10.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(
        group = "notification.halo.run",
        version = "v1alpha1",
        kind = "ReasonType",
        plural = "reasontypes",
        singular = "reasontype")
public class ReasonType extends AbstractExtension {
    public static final String LOCALIZED_RESOURCE_NAME_ANNO = "notification.halo.run/localized-resource-name";

    /** Desired reason type display metadata and attribute definitions. */
    @Schema
    private Spec spec;

    /** Desired reason type metadata and properties available to templates. */
    @Data
    @Schema(name = "ReasonTypeSpec")
    public static class Spec {

        /** Display name shown for this reason type. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String displayName;

        /** Human-readable description of when this reason type is triggered. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String description;

        /** Attribute definitions available on reasons of this type. */
        private List<ReasonProperty> properties;
    }

    /** Attribute definition for reasons of a reason type. */
    @Data
    public static class ReasonProperty {
        /** Attribute name. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String name;

        /** Attribute value type, such as string, number, boolean, or object. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String type;

        /** Human-readable description of the attribute. */
        private String description;

        /** Whether the attribute may be absent from a reason. */
        @Schema(defaultValue = "false")
        private boolean optional;
    }
}
