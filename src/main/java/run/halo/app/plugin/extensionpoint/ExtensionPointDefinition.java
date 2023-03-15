package run.halo.app.plugin.extensionpoint;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * Extension point definition.
 * An {@link ExtensionPointDefinition} is a concept used in <code>Halo</code> to allow for the
 * dynamic extension of system. It defines a location within <code>Halo</code> where
 * additional functionality can be added through the use of plugins or extensions.
 *
 * @author guqing
 * @since 2.4.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = "plugin.halo.run", version = "v1alpha1",
    kind = "ExtensionPointDefinition", singular = "extensionpointdefinition",
    plural = "extensionpointdefinitions")
public class ExtensionPointDefinition extends AbstractExtension {

    @Schema(requiredMode = REQUIRED)
    private ExtensionPointSpec spec;

    @Data
    public static class ExtensionPointSpec {
        @Schema(requiredMode = REQUIRED)
        private String className;

        @Schema(requiredMode = REQUIRED)
        private String displayName;

        @Schema(requiredMode = REQUIRED)
        private ExtensionPointType type;

        private String description;

        private String icon;
    }

    /**
     * <p>Types of extension points include.</p>
     * There are several types:
     * <ul>
     * <li>Singleton extension point: means that only one implementation class of the extension
     * point can be enabled. It is generally used for global core extension points, such as global
     * logging components. When using a singleton extension point, it is necessary to ensure that
     * only one implementation class is enabled, otherwise unexpected issues may occur.</li>
     * <li>Multi-instance extension point: means that there can be multiple implementation
     * classes of the extension point enabled, and the execution order of each implementation
     * class may be different. It is generally used for specific business logic extension points,
     * such as the selection of data sources or the use of caches. When using a multi-instance
     * extension point, it is necessary to consider the dependency relationship and execution
     * order between each implementation class to ensure the correctness of the business logic.</li>
     * <li>Ordered extension point: means that multiple implementation classes of the extension
     * point can be enabled, but they need to be executed in a specified order. It is generally
     * used in scenarios that require strict control of execution order, such as the execution
     * order of message listeners. When using an ordered extension point, it is necessary to
     * assign a priority for each implementation class to ensure that they can be executed in the
     * correct order.</li>
     * <li>Conditional extension point: means that multiple implementation classes of the extension
     * point can be enabled, but they need to meet specific conditions to be executed. For
     * example, some implementation classes can only be executed under specific operating systems
     * or specific runtime environments. When using a conditional extension point, it is
     * necessary to define appropriate conditions according to the actual scenario to ensure the
     * correctness and availability of the extension point.</li>
     * </ul>
     * There are two kinds of definitions for the time being: SINGLETON and MULTI_INSTANCE.
     */
    public enum ExtensionPointType {
        SINGLETON,
        MULTI_INSTANCE;
    }
}
