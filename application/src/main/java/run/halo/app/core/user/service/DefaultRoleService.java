package run.halo.app.core.user.service;

import static run.halo.app.extension.ExtensionUtil.defaultSort;
import static run.halo.app.extension.ExtensionUtil.notDeleting;
import static run.halo.app.security.authorization.AuthorityUtils.containsSuperRole;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import run.halo.app.core.extension.User;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.security.SuperAdminInitializer;

/**
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Service
public class DefaultRoleService implements RoleService {

    private final ReactiveExtensionClient client;

    public DefaultRoleService(ReactiveExtensionClient client) {
        this.client = client;
    }

    private Flux<RoleRef> listRoleRefs(Subject subject) {
        return listRoleBindings(subject).map(RoleBinding::getRoleRef);
    }

    @Override
    public Flux<RoleBinding> listRoleBindings(Subject subject) {
        var listOptions = ListOptions.builder()
            .andQuery(notDeleting())
            .andQuery(QueryFactory.in("subjects", subject.toString()))
            .build();
        return client.listAll(RoleBinding.class, listOptions, defaultSort());
    }

    @Override
    public Flux<String> getRolesByUsername(String username) {
        return listRoleRefs(toUserSubject(username))
            .filter(DefaultRoleService::isRoleKind)
            .map(RoleRef::getName);
    }

    @Override
    public Mono<Map<String, Collection<String>>> getRolesByUsernames(Collection<String> usernames) {
        if (CollectionUtils.isEmpty(usernames)) {
            return Mono.just(Map.of());
        }
        var subjects = usernames.stream().map(DefaultRoleService::toUserSubject)
            .map(Object::toString)
            .collect(Collectors.toSet());
        var listOptions = ListOptions.builder()
            .andQuery(notDeleting())
            .andQuery(QueryFactory.in("subjects", subjects))
            .build();

        return client.listAll(RoleBinding.class, listOptions, defaultSort())
            .collect(HashMap::new, (map, roleBinding) -> {
                for (Subject subject : roleBinding.getSubjects()) {
                    if (subjects.contains(subject.toString())) {
                        var username = subject.getName();
                        var roleRef = roleBinding.getRoleRef();
                        if (isRoleKind(roleRef)) {
                            var roleName = roleRef.getName();
                            map.computeIfAbsent(username, k -> new HashSet<>()).add(roleName);
                        }
                    }
                }
            });
    }

    @Override
    public Mono<Boolean> contains(Collection<String> source, Collection<String> candidates) {
        if (source.contains(SuperAdminInitializer.SUPER_ROLE_NAME)) {
            return Mono.just(true);
        }
        return listWithDependencies(new HashSet<>(source), shouldExcludeHidden(false))
            .map(role -> role.getMetadata().getName())
            .collect(Collectors.toSet())
            .map(roleNames -> roleNames.containsAll(candidates));
    }

    @Override
    public Flux<Role> listPermissions(Set<String> names) {
        if (containsSuperRole(names)) {
            // search all permissions
            return client.listAll(Role.class,
                shouldExcludeHidden(true),
                ExtensionUtil.defaultSort());
        }
        return listWithDependencies(names, shouldExcludeHidden(true));
    }

    @Override
    public Flux<Role> listDependenciesFlux(Set<String> names) {
        return listWithDependencies(names, shouldExcludeHidden(false));
    }

    private static boolean isRoleKind(RoleRef roleRef) {
        return Role.GROUP.equals(roleRef.getApiGroup()) && Role.KIND.equals(roleRef.getKind());
    }

    private static Subject toUserSubject(String username) {
        var subject = new Subject();
        subject.setApiGroup(User.GROUP);
        subject.setKind(User.KIND);
        subject.setName(username);
        return subject;
    }

    private Flux<Role> listRoles(Set<String> names, ListOptions additionalListOptions) {
        if (CollectionUtils.isEmpty(names)) {
            return Flux.empty();
        }

        var listOptions = Optional.ofNullable(additionalListOptions)
            .map(ListOptions::builder)
            .orElseGet(ListOptions::builder)
            .andQuery(notDeleting())
            .andQuery(QueryFactory.in("metadata.name", names))
            .build();

        return client.listAll(Role.class, listOptions, ExtensionUtil.defaultSort());
    }

    private static ListOptions shouldExcludeHidden(boolean excludeHidden) {
        if (!excludeHidden) {
            return null;
        }
        return ListOptions.builder().labelSelector()
            .notEq(Role.HIDDEN_LABEL_NAME, Boolean.TRUE.toString())
            .end()
            .build();
    }

    private Flux<Role> listWithDependencies(Set<String> names, ListOptions additionalListOptions) {
        var visited = new HashSet<String>();
        return listRoles(names, additionalListOptions)
            .expand(role -> {
                var name = role.getMetadata().getName();
                if (visited.contains(name)) {
                    return Flux.empty();
                }
                if (log.isTraceEnabled()) {
                    log.trace("Expand role: {}", role.getMetadata().getName());
                }
                visited.add(name);
                var annotations = MetadataUtil.nullSafeAnnotations(role);
                var dependenciesJson = annotations.get(Role.ROLE_DEPENDENCIES_ANNO);
                var dependencies = stringToList(dependenciesJson);

                return Flux.fromIterable(dependencies)
                    .filter(dep -> !visited.contains(dep))
                    .collect(Collectors.toSet())
                    .flatMapMany(deps -> listRoles(deps, additionalListOptions));
            })
            .concatWith(Flux.defer(() -> listAggregatedRoles(visited, additionalListOptions)));
    }

    private Flux<Role> listAggregatedRoles(Set<String> roleNames,
        ListOptions additionalListOptions) {
        if (CollectionUtils.isEmpty(roleNames)) {
            return Flux.empty();
        }
        var listOptions = Optional.ofNullable(additionalListOptions)
            .map(ListOptions::builder)
            .orElseGet(ListOptions::builder)
            .andQuery(QueryFactory.in("labels.aggregateToRoles", roleNames))
            .build();
        return client.listAll(Role.class, listOptions, ExtensionUtil.defaultSort());
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
        return list(roleNames, false);
    }

    @Override
    public Flux<Role> list(Set<String> roleNames, boolean excludeHidden) {
        if (CollectionUtils.isEmpty(roleNames)) {
            return Flux.empty();
        }
        var builder = ListOptions.builder()
            .andQuery(notDeleting())
            .andQuery(QueryFactory.in("metadata.name", roleNames));
        if (excludeHidden) {
            builder.labelSelector().notEq(Role.HIDDEN_LABEL_NAME, Boolean.TRUE.toString());
        }
        return client.listAll(Role.class, builder.build(), defaultSort());
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
