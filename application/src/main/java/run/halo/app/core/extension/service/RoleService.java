package run.halo.app.core.extension.service;

import java.util.Collection;
import java.util.Set;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding.RoleRef;
import run.halo.app.core.extension.RoleBinding.Subject;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface RoleService {

    Flux<RoleRef> listRoleRefs(Subject subject);

    Mono<Boolean> contains(Collection<String> source, Collection<String> candidates);

    /**
     * This method lists all role templates as permissions recursively according to given role
     * name set.
     *
     * @param names is role name set.
     * @return an array of permissions.
     */
    Flux<Role> listPermissions(Set<String> names);

    Flux<Role> listDependenciesFlux(Set<String> names);

    Flux<Role> list(Set<String> roleNames);
}
