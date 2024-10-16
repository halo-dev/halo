package run.halo.app.core.user.service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.RoleBinding.Subject;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface RoleService {

    Flux<RoleBinding> listRoleBindings(Subject subject);

    Flux<String> getRolesByUsername(String username);

    Mono<Map<String, Collection<String>>> getRolesByUsernames(Collection<String> usernames);

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

    /**
     * List roles by role names.
     *
     * @param roleNames role names
     * @return roles
     */
    Flux<Role> list(Set<String> roleNames);

    /**
     * List roles by role names.
     *
     * @param roleNames role names
     * @param excludeHidden should exclude hidden roles
     * @return roles
     */
    Flux<Role> list(Set<String> roleNames, boolean excludeHidden);
}
