package run.halo.app.core.extension.service;

import static run.halo.app.extension.MetadataUtil.nullSafeLabels;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.RoleBinding.RoleRef;
import run.halo.app.core.extension.RoleBinding.Subject;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;

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
    public Flux<Role> listDependenciesFlux(Set<String> names) {
        if (names == null) {
            return Flux.empty();
        }
        Set<String> visited = new HashSet<>();
        return Flux.fromIterable(names)
            .flatMap(name -> extensionClient.fetch(Role.class, name))
            .expand(role -> {
                var name = role.getMetadata().getName();
                if (visited.contains(name)) {
                    return Flux.empty();
                }
                visited.add(name);
                var annotations = MetadataUtil.nullSafeAnnotations(role);
                var dependenciesJson = annotations.get(Role.ROLE_DEPENDENCIES_ANNO);
                var dependencies = stringToList(dependenciesJson);
                return Flux.fromIterable(dependencies)
                    .filter(dependency -> !visited.contains(dependency))
                    .flatMap(dependencyName -> extensionClient.fetch(Role.class, dependencyName));
            })
            .flatMap(role -> Flux.just(role)
                .mergeWith(listAggregatedRoles(role.getMetadata().getName()))
            );
    }

    Flux<Role> listAggregatedRoles(String roleName) {
        return extensionClient.list(Role.class,
            role -> Boolean.parseBoolean(nullSafeLabels(role)
                .get(Role.ROLE_AGGREGATE_LABEL_PREFIX + roleName)
            ),
            Comparator.comparing(item -> item.getMetadata().getCreationTimestamp()));
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
        return Flux.fromIterable(ObjectUtils.defaultIfNull(roleNames, Set.of()))
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
