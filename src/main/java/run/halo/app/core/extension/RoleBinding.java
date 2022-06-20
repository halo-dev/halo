package run.halo.app.core.extension;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * RoleBinding references a role, but does not contain it.
 * It can reference a Role in the global.
 * It adds who information via Subjects.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@GVK(group = "",
    version = "v1alpha1",
    kind = "RoleBinding",
    plural = "rolebindings",
    singular = "rolebinding")
public class RoleBinding extends AbstractExtension {

    /**
     * Subjects holds references to the objects the role applies to.
     */
    List<Subject> subjects;

    /**
     * RoleRef can reference a Role in the current namespace or a ClusterRole in the global
     * namespace.
     * If the RoleRef cannot be resolved, the Authorizer must return an error.
     */
    RoleRef roleRef;

    /**
     * RoleRef contains information that points to the role being used.
     *
     * @author guqing
     * @since 2.0.0
     */
    @Data
    public static class RoleRef {

        /**
         * Kind is the type of resource being referenced.
         */
        String kind;

        /**
         * Name is the name of resource being referenced.
         */
        String name;

        /**
         * APIGroup is the group for the resource being referenced.
         */
        String apiGroup;
    }

    /**
     * @author guqing
     * @since 2.0.0
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Subject {
        /**
         * Kind of object being referenced. Values defined by this API group are "User", "Group",
         * and "ServiceAccount".
         * If the Authorizer does not recognize the kind value, the Authorizer should report
         * an error.
         */
        String kind;

        /**
         * Name of the object being referenced.
         */
        String name;

        /**
         * APIGroup holds the API group of the referenced subject.
         * Defaults to "" for ServiceAccount subjects.
         * Defaults to "rbac.authorization.halo.run" for User and Group subjects.
         */
        String apiGroup;
    }
}
