package run.halo.app.identity.authorization;

import java.util.List;
import lombok.Data;
import run.halo.app.infra.types.ObjectMeta;
import run.halo.app.infra.types.TypeMeta;

/**
 * RoleBinding references a role, but does not contain it.
 *
 * @author guqing
 * @see
 * <a href="https://github.com/kubernetes/kubernetes/blob/537941765fe1304dd096c1a2d4d4e70f10768218/pkg/apis/rbac/types.go#L109">types#RoleBinding</a>
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
}
