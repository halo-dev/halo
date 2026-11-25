package run.halo.app.core.user.service;

import static run.halo.app.extension.ExtensionUtil.defaultSort;
import static run.halo.app.extension.ExtensionUtil.notDeleting;
import static run.halo.app.security.authorization.AuthorityUtils.SUPER_ROLE_NAME;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JacksonUtils;
import tools.jackson.core.type.TypeReference;

/**
 * Resolves which directly assigned roles effectively contain a required role name.
 *
 * <p>A role effectively contains another role when the required role is reachable through the role dependency graph,
 * e.g. {@code post-editor -> role-template-manage-posts -> role-template-post-author ->
 * role-template-post-contributor}. The super role is always treated as containing every role.
 *
 * <p>This is the inverse of {@link RoleService#contains(Collection, Collection)}: given a required role, it returns the
 * role names that can be directly assigned to a user and still get the required role, so that callers can filter users
 * by the role index.
 *
 * @since 2.28.0
 */
@Component
@RequiredArgsConstructor
public class EffectiveRoleResolver {

    private static final TypeReference<Set<String>> ROLE_NAMES_TYPE = new TypeReference<>() {};

    private final ReactiveExtensionClient client;

    /**
     * Resolves all directly assignable role names whose effective roles contain the required role.
     *
     * @param requiredRoleName the required role name, e.g. {@code role-template-post-contributor}
     * @return role names, or an empty set when the required role does not exist
     */
    public Mono<Set<String>> resolveRolesContaining(String requiredRoleName) {
        if (StringUtils.isBlank(requiredRoleName)) {
            return Mono.just(Set.of());
        }
        var listOptions = ListOptions.builder().andQuery(notDeleting()).build();
        return client.listAll(Role.class, listOptions, defaultSort())
                .collectList()
                .map(roles -> resolveRolesContaining(requiredRoleName, roles));
    }

    Set<String> resolveRolesContaining(String requiredRoleName, Collection<Role> roles) {
        var roleMap = roles.stream()
                .filter(role -> role.getMetadata() != null)
                .filter(role -> StringUtils.isNotBlank(role.getMetadata().getName()))
                .collect(Collectors.toMap(
                        role -> role.getMetadata().getName(), role -> role, (left, right) -> left, LinkedHashMap::new));

        if (!roleMap.containsKey(requiredRoleName)) {
            return Set.of();
        }

        return roleMap.keySet().stream()
                .filter(roleName -> SUPER_ROLE_NAME.equals(roleName)
                        || dependencyClosure(roleName, roleMap).contains(requiredRoleName))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> dependencyClosure(String roleName, Map<String, Role> roleMap) {
        var visited = new HashSet<String>();
        var queue = new ArrayDeque<String>();
        queue.add(roleName);

        while (!queue.isEmpty()) {
            var current = queue.removeFirst();
            if (!visited.add(current)) {
                continue;
            }
            var role = roleMap.get(current);
            if (role == null) {
                continue;
            }
            dependencies(role).stream()
                    .filter(dependency -> !visited.contains(dependency))
                    .forEach(queue::add);
        }

        return visited;
    }

    private Set<String> dependencies(Role role) {
        var dependenciesJson = Optional.ofNullable(role.getMetadata())
                .map(metadata -> metadata.getAnnotations())
                .map(annotations -> annotations.get(Role.ROLE_DEPENDENCIES_ANNO))
                .filter(StringUtils::isNotBlank)
                .orElse(null);
        if (dependenciesJson == null) {
            return Set.of();
        }
        return JacksonUtils.MAPPER.readValue(dependenciesJson, ROLE_NAMES_TYPE);
    }
}
