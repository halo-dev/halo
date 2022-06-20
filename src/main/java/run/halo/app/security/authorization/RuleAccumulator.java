package run.halo.app.security.authorization;

import run.halo.app.core.extension.Role;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface RuleAccumulator {
    boolean visit(String source, Role.PolicyRule rule, Throwable err);
}
