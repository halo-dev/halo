package run.halo.app.identity.authorization;

import java.util.List;
import lombok.Data;
import run.halo.app.infra.types.ObjectMeta;
import run.halo.app.infra.types.TypeMeta;

/**
 * @author guqing
 * @since 2.0.0
 */
@Data
public class Role {

    TypeMeta typeMeta;

    ObjectMeta objectMeta;

    List<PolicyRule> rules;
}
