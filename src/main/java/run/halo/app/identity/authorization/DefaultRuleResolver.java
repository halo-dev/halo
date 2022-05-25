package run.halo.app.identity.authorization;

import java.util.Collections;
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
        List<RoleBinding> roleBindings = Collections.emptyList();
        try {
            roleBindings = roleBindingLister.listRoleBindings();
        } catch (Exception e) {
            if (visitor.visit(null, null, e)) {
                return;
            }
        }

        for (RoleBinding roleBinding : roleBindings) {
            AppliesResult appliesResult = appliesTo(user, roleBinding.subjects);
            if (!appliesResult.applies) {
                continue;
            }

            Subject subject = roleBinding.subjects.get(appliesResult.subjectIndex);

            List<PolicyRule> rules = Collections.emptyList();
            try {
                Role role = roleGetter.getRole(roleBinding.roleRef.name);
                rules = role.getRules();
            } catch (Exception e) {
                if (visitor.visit(null, null, e)) {
                    return;
                }
            }

            String source = roleBindingDescriber(roleBinding, subject);
            for (PolicyRule rule : rules) {
                if (!visitor.visit(source, rule, null)) {
                    return;
                }
            }
        }
    }

    String roleBindingDescriber(RoleBinding roleBinding, Subject subject) {
        String describeSubject = String.format("%s %s", subject.kind, subject.name);
        return String.format("RoleBinding %s of %s %s to %s", roleBinding.metadata().getName(),
            roleBinding.roleRef.getKind(), roleBinding.roleRef.getName(), describeSubject);
    }

    AppliesResult appliesTo(UserDetails user, List<Subject> bindingSubjects) {
        for (int i = 0; i < bindingSubjects.size(); i++) {
            if (appliesToUser(user, bindingSubjects.get(i))) {
                return new AppliesResult(true, i);
            }
        }
        return new AppliesResult(false, 0);
    }

    boolean appliesToUser(UserDetails user, Subject subject) {
        if (USER_KIND.equals(subject.kind)) {
            return StringUtils.equals(user.getUsername(), subject.name);
        }
        return false;
    }

    record AppliesResult(boolean applies, int subjectIndex) {
    }
}
