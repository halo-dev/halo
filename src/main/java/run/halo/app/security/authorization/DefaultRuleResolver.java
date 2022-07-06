package run.halo.app.security.authorization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.reconciler.RoleReconciler;
import run.halo.app.core.extension.service.DefaultRoleBindingService;
import run.halo.app.core.extension.service.RoleBindingService;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.infra.utils.JsonUtils;

/**
 * @author guqing
 * @since 2.0.0
 */
@Data
public class DefaultRuleResolver implements AuthorizationRuleResolver {

    private RoleService roleService;

    private RoleBindingService roleBindingService = new DefaultRoleBindingService();

    public DefaultRuleResolver(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public PolicyRuleList rulesFor(UserDetails user) {
        PolicyRuleList policyRules = new PolicyRuleList();
        visitRulesFor(user, (source, rule, err) -> {
            if (rule != null) {
                policyRules.add(rule);
            }
            if (err != null) {
                policyRules.addError(err);
            }
            return true;
        });
        return policyRules;
    }

    @Override
    public void visitRulesFor(UserDetails user, RuleAccumulator visitor) {
        Set<String> roleNames = roleBindingService.listBoundRoleNames(user.getAuthorities());

        List<Role.PolicyRule> rules = Collections.emptyList();
        for (String roleName : roleNames) {
            try {
                Role role = roleService.getRole(roleName);
                // fetch rules from role
                rules = fetchRules(role);
            } catch (Exception e) {
                if (visitor.visit(null, null, e)) {
                    return;
                }
            }

            String source = roleBindingDescriber(roleName, user.getUsername());
            for (Role.PolicyRule rule : rules) {
                if (!visitor.visit(source, rule, null)) {
                    return;
                }
            }
        }
    }

    private List<Role.PolicyRule> fetchRules(Role role) {
        Map<String, String> annotations = role.getMetadata().getAnnotations();
        if (annotations == null) {
            return role.getRules();
        }
        // merge policy rules
        String roleDependencyRules = annotations.get(RoleReconciler.ROLE_DEPENDENCY_RULES);
        List<Role.PolicyRule> rules = convertFrom(roleDependencyRules);
        rules.addAll(role.getRules());
        return rules;
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

    String roleBindingDescriber(String roleName, String subject) {
        return String.format("Binding role [%s] to [%s]", roleName, subject);
    }

    public void setRoleBindingService(RoleBindingService roleBindingService) {
        Assert.notNull(roleBindingService, "The roleBindingLister must not be null.");
        this.roleBindingService = roleBindingService;
    }
}
