package run.halo.app.identity.authorization;

import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import run.halo.app.infra.types.ObjectMeta;
import run.halo.app.infra.types.TypeMeta;

/**
 * // RoleBinding references a role, but does not contain it.  It can reference a Role in the
 * same namespace or a ClusterRole in the global namespace.
 * // It adds who information via Subjects and namespace information by which namespace it exists
 * in.  RoleBindings in a given
 * // namespace only have effect in that namespace.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
public class RoleBinding {

    TypeMeta typeMeta;

    ObjectMeta objectMeta;

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

    public String getName() {
        if (objectMeta == null) {
            return StringUtils.EMPTY;
        }
        return objectMeta.getName();
    }
}
