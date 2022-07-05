package run.halo.app.core.extension.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.RoleBinding.RoleRef;
import run.halo.app.core.extension.RoleBinding.Subject;
import run.halo.app.core.extension.reconciler.RoleReconciler;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.infra.utils.JsonUtils;

/**
 * @author guqing
 * @since 2.0.0
 */
@Service
public class DefaultRoleService implements RoleService {

    private final ExtensionClient extensionClient;

    public DefaultRoleService(ExtensionClient extensionClient) {
        this.extensionClient = extensionClient;
    }

    @Override
    @NonNull
    public Role getRole(@NonNull String name) {
        return extensionClient.fetch(Role.class, name)
            .map(role -> {
                Map<String, String> annotations = role.getMetadata().getAnnotations();
                if (annotations == null) {
                    return role;
                }
                // merge policy rules
                String roleDependencyRules = annotations.get(RoleReconciler.ROLE_DEPENDENCY_RULES);
                List<Role.PolicyRule> rules = convertFrom(roleDependencyRules);
                rules.addAll(role.getRules());

                role.setRules(rules);
                return role;
            }).orElseThrow();
    }

    private List<Role.PolicyRule> convertFrom(String json) {
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

    @Override
    public Flux<RoleRef> listRoleRefs(Subject subject) {
        return Flux.fromIterable(extensionClient.list(RoleBinding.class,
                binding -> binding.getSubjects().contains(subject),
                null))
            .map(RoleBinding::getRoleRef);
    }
}
