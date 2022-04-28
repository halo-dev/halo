package run.halo.app.identity.authorization;

import java.util.List;
import lombok.Data;
import run.halo.app.infra.types.ObjectMeta;
import run.halo.app.infra.types.TypeMeta;

/**
 * Role logical grouping of PolicyRules that can be referenced as a unit by a RoleBinding.
 *
 * @author guqing
 * @see
 * <a href="https://github.com/kubernetes/kubernetes/blob/537941765fe1304dd096c1a2d4d4e70f10768218/pkg/apis/rbac/types.go#L95">types#Role</a>
 * @since 2.0.0
 */
@Data
public class Role {

    TypeMeta typeMeta;

    ObjectMeta objectMeta;

    List<PolicyRule> rules;
}
