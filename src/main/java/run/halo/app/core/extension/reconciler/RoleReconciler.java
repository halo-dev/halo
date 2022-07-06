package run.halo.app.core.extension.reconciler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import run.halo.app.core.extension.Role;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Role reconcile.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class RoleReconciler implements Reconciler {
    public static final String ROLE_DEPENDENCIES = "halo.run/dependencies";
    public static final String ROLE_DEPENDENCY_RULES = "halo.run/dependency-rules";

    private final ExtensionClient client;

    public RoleReconciler(ExtensionClient client) {
        this.client = client;
    }

    @Override
    public Result reconcile(Request request) {
        client.fetch(Role.class, request.name()).ifPresent(role -> {
            Map<String, String> annotations = role.getMetadata().getAnnotations();
            if (annotations == null) {
                annotations = new HashMap<>();
                role.getMetadata().setAnnotations(annotations);
            }
            Map<String, String> oldAnnotations = Map.copyOf(annotations);

            String s = annotations.get(ROLE_DEPENDENCIES);
            List<String> roleDependencies = readValue(s);
            List<Role.PolicyRule> dependencyRules = listDependencyRoles(roleDependencies)
                .stream()
                .map(Role::getRules)
                .flatMap(List::stream)
                .sorted()
                .toList();
            // override dependency rules to annotations
            annotations.put(ROLE_DEPENDENCY_RULES, JsonUtils.objectToJson(dependencyRules));
            if (!Objects.deepEquals(oldAnnotations, annotations)) {
                client.update(role);
            }
        });
        return new Result(false, null);
    }

    private List<String> readValue(String json) {
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            return JsonUtils.DEFAULT_JSON_MAPPER.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Role> listDependencyRoles(List<String> dependencies) {
        List<Role> result = new ArrayList<>();
        if (dependencies == null) {
            return result;
        }
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>(dependencies);
        while (!queue.isEmpty()) {
            String roleName = queue.poll();
            // detecting cycle in role dependencies
            if (visited.contains(roleName)) {
                log.warn("Detected a cycle in role dependencies: {},and skipped automatically",
                    roleName);
                continue;
            }
            visited.add(roleName);
            client.fetch(Role.class, roleName).ifPresent(role -> {
                result.add(role);
                // add role dependencies to queue
                Map<String, String> annotations = role.getMetadata().getAnnotations();
                if (annotations != null) {
                    String roleNameDependencies = annotations.get(ROLE_DEPENDENCIES);
                    List<String> roleDependencies = readValue(roleNameDependencies);
                    queue.addAll(roleDependencies);
                }
            });
        }
        return result;
    }
}
