package run.halo.app.core.user.service;

import static run.halo.app.extension.ExtensionUtil.defaultSort;
import static run.halo.app.extension.ExtensionUtil.notDeleting;
import static run.halo.app.security.authorization.AuthorityUtils.SUPER_ROLE_NAME;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
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
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;

@Component
@RequiredArgsConstructor
public class EffectiveRoleResolver {

    private final ReactiveExtensionClient client;

    public Mono<Set<String>> resolveRolesContaining(String roleName) {
        if (StringUtils.isBlank(roleName)) {
            return Mono.just(Set.of());
        }

        var listOptions = ListOptions.builder().andQuery(notDeleting()).build();
        return client.listAll(Role.class, listOptions, defaultSort())
                .collectList()
                .map(roles -> resolveRolesContaining(roleName, roles));
    }

    Set<String> resolveRolesContaining(String roleName, Collection<Role> roles) {
        var roleMap = roles.stream()
                .filter(role -> role.getMetadata() != null)
                .filter(role -> StringUtils.isNotBlank(role.getMetadata().getName()))
                .collect(Collectors.toMap(
                        role -> role.getMetadata().getName(), role -> role, (left, right) -> left, LinkedHashMap::new));

        if (!roleMap.containsKey(roleName)) {
            return Set.of();
        }

        var aggregateToRoles = aggregateToRoles(roleMap.values());
        return roleMap.keySet().stream()
                .filter(name -> SUPER_ROLE_NAME.equals(name)
                        || effectiveRoleNames(name, roleMap, aggregateToRoles).contains(roleName))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> effectiveRoleNames(
            String roleName, Map<String, Role> roleMap, Map<String, Set<String>> aggregateToRoles) {
        var visited = new LinkedHashSet<String>();
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

            dependencies(role).stream().filter(dep -> !visited.contains(dep)).forEach(queue::add);
            aggregateToRoles.getOrDefault(current, Set.of()).stream()
                    .filter(aggregatedRole -> !visited.contains(aggregatedRole))
                    .forEach(queue::add);
        }

        return visited;
    }

    private Map<String, Set<String>> aggregateToRoles(Collection<Role> roles) {
        var result = new HashMap<String, Set<String>>();
        roles.forEach(role -> {
            var roleName = role.getMetadata().getName();
            MetadataUtil.nullSafeLabels(role).forEach((key, value) -> {
                if (!key.startsWith(Role.ROLE_AGGREGATE_LABEL_PREFIX) || !Boolean.parseBoolean(value)) {
                    return;
                }
                var targetRole = StringUtils.removeStart(key, Role.ROLE_AGGREGATE_LABEL_PREFIX);
                result.computeIfAbsent(targetRole, ignored -> new LinkedHashSet<>())
                        .add(roleName);
            });
        });
        return result;
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

        return JsonUtils.jsonToObject(dependenciesJson, new TypeReference<HashSet<String>>() {});
    }
}
