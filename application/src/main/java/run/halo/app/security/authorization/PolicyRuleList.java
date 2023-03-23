package run.halo.app.security.authorization;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import run.halo.app.core.extension.Role;

/**
 * @author guqing
 * @since 2.0.0
 */
public class PolicyRuleList extends LinkedList<Role.PolicyRule> {
    private final List<Throwable> errors = new ArrayList<>(4);

    /**
     * @return true if an error occurred when parsing PolicyRules
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<Throwable> getErrors() {
        return errors;
    }

    public PolicyRuleList addError(Throwable error) {
        errors.add(error);
        return this;
    }

    public PolicyRuleList addErrors(List<Throwable> errors) {
        this.errors.addAll(errors);
        return this;
    }
}
