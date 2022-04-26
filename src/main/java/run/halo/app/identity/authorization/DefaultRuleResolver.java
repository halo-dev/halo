package run.halo.app.identity.authorization;

import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author guqing
 * @since 2.0.0
 */
@Data
public class DefaultRuleResolver implements AuthorizationRuleResolver {
    private static final String USER_KIND = "User";
    RoleGetter roleGetter;
    RoleBindingLister roleBindingLister;

    public DefaultRuleResolver(RoleGetter roleGetter, RoleBindingLister roleBindingLister) {
        this.roleGetter = roleGetter;
        this.roleBindingLister = roleBindingLister;
    }

    @Override
    public PolicyRuleList rulesFor(UserDetails user) {
        return visitRulesFor(user);
    }

    protected PolicyRuleList visitRulesFor(UserDetails user) {
        PolicyRuleList rules = new PolicyRuleList();
        try {
            List<RoleBinding> roleBindings = roleBindingLister.listRoleBindings();
            for (RoleBinding roleBinding : roleBindings) {
                boolean applies = appliesTo(user, roleBinding.subjects);
                if (!applies) {
                    continue;
                }
                Role role = roleGetter.getRole(roleBinding.roleRef.name);
                if (role == null) {
                    return rules;
                }
                rules.addAll(role.getRules());
            }
        } catch (Exception e) {
            // ignore throws
            rules.addError(e);
        }

        return rules;
    }

    boolean appliesTo(UserDetails user, List<Subject> bindingSubjects) {
        for (Subject bindingSubject : bindingSubjects) {
            if (appliesToUser(user, bindingSubject)) {
                return true;
            }
        }
        return false;
    }

    boolean appliesToUser(UserDetails user, Subject subject) {
        if (USER_KIND.equals(subject.kind)) {
            return StringUtils.equals(user.getUsername(), subject.name);
        }
        return false;
    }
}
