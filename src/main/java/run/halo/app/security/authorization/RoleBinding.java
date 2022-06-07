package run.halo.app.security.authorization;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
@GVK(group = "", version = "v1alpha1", kind = "RoleBinding", plural = "rolebindings",
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
}
