package run.halo.app.core.extension.service;

import static run.halo.app.extension.Comparators.compareCreationTimestamp;
import static run.halo.app.security.authorization.AuthorityUtils.containsSuperRole;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.RoleBinding.RoleRef;
import run.halo.app.core.extension.RoleBinding.Subject;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.security.SuperAdminInitializer;

/**
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Service
public class DefaultRoleService implements RoleService {

    private final ReactiveExtensionClient extensionClient;

    public DefaultRoleService(ReactiveExtensionClient extensionClient) {
        this.extensionClient = extensionClient;
    }

    @Override
    public Flux<RoleRef> listRoleRefs(Subject subject) {
        return extensionClient.list(RoleBinding.class,
                binding -> binding.getSubjects().contains(subject),
                null)
            .map(RoleBinding::getRoleRef);
    }

    @Override
    public Mono<Boolean> contains(Collection<String> source, Collection<String> candidates) {
        if (source.contains(SuperAdminInitializer.SUPER_ROLE_NAME)) {
            return Mono.just(true);
        }
        return listDependencies(new HashSet<>(source), shouldFilterHidden(false))
            .map(role -> role.getMetadata().getName())
            .collect(Collectors.toSet())
            .map(roleNames -> roleNames.containsAll(candidates));
    }

    @Override
    public Flux<Role> listPermissions(Set<String> names) {
        if (containsSuperRole(names)) {
            // search all permissions
            return extensionClient.list(Role.class,
                    shouldFilterHidden(true),
                    compareCreationTimestamp(true))
                .filter(DefaultRoleService::isRoleTemplate);
        }
        return listDependencies(names, shouldFilterHidden(true))
            .filter(DefaultRoleService::isRoleTemplate);
    }

    @Override
    public Flux<Role> listDependenciesFlux(Set<String> names) {
        return listDependencies(names, shouldFilterHidden(false));
    }

    private Flux<Role> listRoles(Set<String> names, Predicate<Role> additionalPredicate) {
        if (CollectionUtils.isEmpty(names)) {
            return Flux.empty();
        }

        Predicate<Role> predicate = role -> names.contains(role.getMetadata().getName());
        if (additionalPredicate != null) {
            predicate = predicate.and(additionalPredicate);
        }
        return extensionClient.list(Role.class, predicate, compareCreationTimestamp(true));
    }

    private static Predicate<Role> shouldFilterHidden(boolean filterHidden) {
        if (!filterHidden) {
            return r -> true;
        }
        return role -> {
            var labels = role.getMetadata().getLabels();
            if (labels == null) {
                return true;
            }
            var hiddenValue = labels.get(Role.HIDDEN_LABEL_NAME);
            return !Boolean.parseBoolean(hiddenValue);
        };
    }

    private static boolean isRoleTemplate(Role role) {
        var labels = role.getMetadata().getLabels();
        if (labels == null) {
            return false;
        }
        return Boolean.parseBoolean(labels.get(Role.TEMPLATE_LABEL_NAME));
    }

    private Flux<Role> listDependencies(Set<String> names, Predicate<Role> additionalPredicate) {
        var visited = new HashSet<String>();
        return listRoles(names, additionalPredicate)
            .expand(role -> {
                var name = role.getMetadata().getName();
                if (visited.contains(name)) {
                    return Flux.empty();
                }
                log.debug("Expand role: {}", role.getMetadata().getName());
                visited.add(name);
                var annotations = MetadataUtil.nullSafeAnnotations(role);
                var dependenciesJson = annotations.get(Role.ROLE_DEPENDENCIES_ANNO);
                var dependencies = stringToList(dependenciesJson);

                return Flux.fromIterable(dependencies)
                    .filter(dep -> !visited.contains(dep))
                    .collect(Collectors.<String>toSet())
                    .flatMapMany(deps -> listRoles(deps, additionalPredicate));
            })
            .concatWith(Flux.defer(() -> listAggregatedRoles(visited, additionalPredicate)));
    }

    private Flux<Role> listAggregatedRoles(Set<String> roleNames,
        Predicate<Role> additionalPredicate) {
        var aggregatedLabelNames = roleNames.stream()
            .map(roleName -> Role.ROLE_AGGREGATE_LABEL_PREFIX + roleName)
            .collect(Collectors.toSet());
        Predicate<Role> predicate = role -> {
            var labels = role.getMetadata().getLabels();
            if (labels == null) {
                return false;
            }
            return aggregatedLabelNames.stream()
                .anyMatch(aggregatedLabel -> Boolean.parseBoolean(labels.get(aggregatedLabel)));
        };
        if (additionalPredicate != null) {
            predicate = predicate.and(additionalPredicate);
        }
        return extensionClient.list(Role.class, predicate, compareCreationTimestamp(true));
    }

    Predicate<RoleBinding> getRoleBindingPredicate(Subject targetSubject) {
        return roleBinding -> {
            List<Subject> subjects = roleBinding.getSubjects();
            for (Subject subject : subjects) {
                return matchSubject(targetSubject, subject);
            }
            return false;
        };
    }

    private static boolean matchSubject(Subject targetSubject, Subject subject) {
        if (targetSubject == null || subject == null) {
            return false;
        }
        return StringUtils.equals(targetSubject.getKind(), subject.getKind())
            && StringUtils.equals(targetSubject.getName(), subject.getName())
            && StringUtils.defaultString(targetSubject.getApiGroup())
            .equals(StringUtils.defaultString(subject.getApiGroup()));
    }

    @Override
    public Flux<Role> list(Set<String> roleNames) {
        if (CollectionUtils.isEmpty(roleNames)) {
            return Flux.empty();
        }
        return Flux.fromIterable(roleNames)
            .flatMap(roleName -> extensionClient.fetch(Role.class, roleName));
    }

    @NonNull
    private List<String> stringToList(String str) {
        if (StringUtils.isBlank(str)) {
            return Collections.emptyList();
        }
        return JsonUtils.jsonToObject(str,
            new TypeReference<>() {
            });
    }
}
