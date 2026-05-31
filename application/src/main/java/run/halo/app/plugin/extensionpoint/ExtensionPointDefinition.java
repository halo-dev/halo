package run.halo.app.plugin.extensionpoint;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * Extension point definition that advertises where plugins can contribute implementations.
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
        kind = "ExtensionPointDefinition",
        singular = "extensionpointdefinition",
        plural = "extensionpointdefinitions")
public class ExtensionPointDefinition extends AbstractExtension {

    /** Desired extension point metadata and registration behavior. */
    @Schema(requiredMode = REQUIRED)
    private ExtensionPointSpec spec;

    /** Desired extension point metadata. */
    @Data
    public static class ExtensionPointSpec {
        /** Fully qualified Java class name of the extension point interface. */
        @Schema(requiredMode = REQUIRED)
        private String className;

        /** Display name shown for the extension point. */
        @Schema(requiredMode = REQUIRED)
        private String displayName;

        /** Registration mode used when loading implementations for this extension point. */
        @Schema(requiredMode = REQUIRED)
        private ExtensionPointType type;

        /** Human-readable description of what this extension point allows plugins to provide. */
        private String description;

        /** Icon URL, class, or identifier shown for the extension point. */
        private String icon;
    }

    /**
     * Types of extension points include. There are several types:
     *
     * <ul>
     *   <li>Singleton extension point: means that only one implementation class of the extension point can be enabled.
     *       It is generally used for global core extension points, such as global logging components. When using a
     *       singleton extension point, it is necessary to ensure that only one implementation class is enabled,
     *       otherwise unexpected issues may occur.
     *   <li>Multi-instance extension point: means that there can be multiple implementation classes of the extension
     *       point enabled, and the execution order of each implementation class may be different. It is generally used
     *       for specific business logic extension points, such as the selection of data sources or the use of caches.
     *       When using a multi-instance extension point, it is necessary to consider the dependency relationship and
     *       execution order between each implementation class to ensure the correctness of the business logic.
     *   <li>Ordered extension point: means that multiple implementation classes of the extension point can be enabled,
     *       but they need to be executed in a specified order. It is generally used in scenarios that require strict
     *       control of execution order, such as the execution order of message listeners. When using an ordered
     *       extension point, it is necessary to assign a priority for each implementation class to ensure that they can
     *       be executed in the correct order.
     *   <li>Conditional extension point: means that multiple implementation classes of the extension point can be
     *       enabled, but they need to meet specific conditions to be executed. For example, some implementation classes
     *       can only be executed under specific operating systems or specific runtime environments. When using a
     *       conditional extension point, it is necessary to define appropriate conditions according to the actual
     *       scenario to ensure the correctness and availability of the extension point.
     * </ul>
     *
     * There are two kinds of definitions for the time being: SINGLETON and MULTI_INSTANCE.
     */
    public enum ExtensionPointType {
        SINGLETON,
        MULTI_INSTANCE;
    }
}
