package run.halo.app.security.authorization;

import java.util.ArrayList;
import java.util.List;
import run.halo.app.core.extension.Role;

/**
 * authorizing visitor short-circuits once allowed, and collects any resolution errors encountered.
 *
 * @author guqing
 * @since 2.0.0
 */
class AuthorizingVisitor implements RuleAccumulator {
    private final RbacRequestEvaluation requestEvaluation = new RbacRequestEvaluation();

    private final Attributes requestAttributes;

    private boolean allowed;

    private String reason;

    private final List<Throwable> errors = new ArrayList<>(4);

    public AuthorizingVisitor(Attributes requestAttributes) {
        this.requestAttributes = requestAttributes;
    }

    @Override
    public boolean visit(String source, Role.PolicyRule rule, Throwable error) {
        if (rule != null && requestEvaluation.ruleAllows(requestAttributes, rule)) {
            this.allowed = true;
            this.reason = String.format("RBAC: allowed by %s", source);
            return false;
        }
        if (error != null) {
            this.errors.add(error);
        }
        return true;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public String getReason() {
        return reason;
    }

    public List<Throwable> getErrors() {
        return errors;
    }
}
