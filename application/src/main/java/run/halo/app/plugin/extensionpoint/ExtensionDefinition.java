package run.halo.app.plugin.extensionpoint;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * Extension definition that describes one plugin-provided implementation of an extension point.
 *
 * @author guqing
 * @since 2.4.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(
        group = "plugin.halo.run",
        version = "v1alpha1",
        kind = "ExtensionDefinition",
        singular = "extensiondefinition",
        plural = "extensiondefinitions")
public class ExtensionDefinition extends AbstractExtension {

    /** Desired extension implementation metadata. */
    @Schema(requiredMode = REQUIRED)
    private ExtensionSpec spec;

    /** Desired plugin-provided extension implementation metadata. */
    @Data
    public static class ExtensionSpec {
        /** Fully qualified Java class name of the implementation. */
        @Schema(requiredMode = REQUIRED)
        private String className;

        /** ExtensionPointDefinition metadata.name this implementation contributes to. */
        @Schema(requiredMode = REQUIRED)
        private String extensionPointName;

        /** Display name shown for the implementation. */
        @Schema(requiredMode = REQUIRED)
        private String displayName;

        /** Human-readable description of what this implementation does. */
        private String description;

        /** Icon URL, class, or identifier shown for the implementation. */
        private String icon;
    }
}
