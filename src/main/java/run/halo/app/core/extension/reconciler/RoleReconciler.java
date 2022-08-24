package run.halo.app.core.extension.reconciler;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Role reconcile.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class RoleReconciler implements Reconciler<Request> {

    private final ExtensionClient client;

    private final RoleService roleService;

    public RoleReconciler(ExtensionClient client, RoleService roleService) {
        this.client = client;
        this.roleService = roleService;
    }

    @Override
    public Result reconcile(Request request) {
        client.fetch(Role.class, request.name()).ifPresent(role -> {
            final Role oldRole = JsonUtils.deepCopy(role);
            Map<String, String> annotations = role.getMetadata().getAnnotations();
            if (annotations == null) {
                annotations = new HashMap<>();
                role.getMetadata().setAnnotations(annotations);
            }

            Set<String> roleDependencies = readValue(annotations.get(Role.ROLE_DEPENDENCIES_ANNO));

            List<Role> dependenciesRole = roleService.listDependencies(roleDependencies);

            List<Role.PolicyRule> dependencyRules = dependenciesRole.stream()
                .map(Role::getRules)
                .flatMap(List::stream)
                .sorted()
                .toList();
            List<String> uiPermissions = aggregateUiPermissions(dependenciesRole);
            // override dependency rules to annotations
            annotations.put(Role.ROLE_DEPENDENCY_RULES, JsonUtils.objectToJson(dependencyRules));
            annotations.put(Role.UI_PERMISSIONS_AGGREGATED_ANNO,
                JsonUtils.objectToJson(uiPermissions));
            if (!Objects.equals(oldRole, role)) {
                client.update(role);
            }
        });
        return new Result(false, null);
    }

    private List<String> aggregateUiPermissions(List<Role> dependencyRoles) {
        return dependencyRoles.stream()
            .filter(role -> role.getMetadata().getAnnotations() != null)
            .map(role -> {
                Map<String, String> roleAnnotations = role.getMetadata().getAnnotations();
                return roleAnnotations.get(Role.UI_PERMISSIONS_ANNO);
            })
            .map(this::readValue)
            .flatMap(Set::stream)
            .sorted()
            .toList();
    }

    private Set<String> readValue(String json) {
        if (StringUtils.isBlank(json)) {
            return new LinkedHashSet<>();
        }
        return JsonUtils.jsonToObject(json, new TypeReference<>() {
        });
    }

}
