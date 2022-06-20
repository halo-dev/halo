package run.halo.app.core.extension.service;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding.RoleRef;
import run.halo.app.core.extension.RoleBinding.Subject;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface RoleService {

    @NonNull
    Role getRole(String name);

    Flux<RoleRef> listRoleRefs(Subject subject);
}
