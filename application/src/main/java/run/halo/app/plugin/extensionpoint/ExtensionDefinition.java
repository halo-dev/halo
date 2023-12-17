package run.halo.app.plugin.extensionpoint;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * Extension definition.
 * An {@link ExtensionDefinition} is a type of metadata that provides additional information about
 * an extension. An extension is a way to add new functionality to an existing class, structure,
 * enumeration, or protocol type without needing to subclass it.
 *
 * @author guqing
 * @since 2.4.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = "plugin.halo.run", version = "v1alpha1",
    kind = "ExtensionDefinition", singular = "extensiondefinition",
    plural = "extensiondefinitions")
public class ExtensionDefinition extends AbstractExtension {

    @Schema(requiredMode = REQUIRED)
    private ExtensionSpec spec;

    @Data
    public static class ExtensionSpec {
        @Schema(requiredMode = REQUIRED)
        private String className;

        @Schema(requiredMode = REQUIRED)
        private String extensionPointName;

        @Schema(requiredMode = REQUIRED)
        private String displayName;

        private String description;

        private String icon;
    }
}
