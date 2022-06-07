package run.halo.app.core.extension.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.Data;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;
import run.halo.app.core.extension.Role;
import run.halo.app.security.authorization.AuthorizationRuleResolver;
import run.halo.app.security.authorization.DefaultRoleBindingLister;
import run.halo.app.security.authorization.PolicyRuleList;
import run.halo.app.security.authorization.RuleAccumulator;

/**
 * @author guqing
 * @since 2.0.0
 */
@Data
public class DefaultRuleResolver implements AuthorizationRuleResolver {

    private RoleGetter roleGetter;

    private RoleBindingLister roleBindingLister = new DefaultRoleBindingLister();

    public DefaultRuleResolver(RoleGetter roleGetter) {
        this.roleGetter = roleGetter;
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
        Set<String> roleNames = roleBindingLister.listBoundRoleNames(user.getAuthorities());

        List<Role.PolicyRule> rules = Collections.emptyList();
        for (String roleName : roleNames) {
            try {
                Role role = roleGetter.getRole(roleName);
                rules = role.getRules();
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

    String roleBindingDescriber(String roleName, String subject) {
        return String.format("Binding role [%s] to [%s]", roleName, subject);
    }

    public void setRoleBindingLister(RoleBindingLister roleBindingLister) {
        Assert.notNull(roleBindingLister, "The roleBindingLister must not be null.");
        this.roleBindingLister = roleBindingLister;
    }
}
