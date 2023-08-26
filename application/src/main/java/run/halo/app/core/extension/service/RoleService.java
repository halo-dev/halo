package run.halo.app.core.extension.service;

import java.util.Set;
import reactor.core.publisher.Flux;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding.RoleRef;
import run.halo.app.core.extension.RoleBinding.Subject;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface RoleService {

    Flux<RoleRef> listRoleRefs(Subject subject);

    Flux<Role> listDependenciesFlux(Set<String> names);

    Flux<Role> list(Set<String> roleNames);
}
