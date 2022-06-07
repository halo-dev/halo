package run.halo.app.security.authorization;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface RuleAccumulator {
    boolean visit(String source, PolicyRule rule, Throwable err);
}
