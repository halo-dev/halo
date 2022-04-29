package run.halo.app.identity.authorization;

import java.util.List;

/**
 * @author guqing
 * @since 2.0.0
 */
public class PolicyRuleInfo {
    List<ResourceRuleInfo> resourceRules;
    List<NonResourceRuleInfo> nonResourceRules;
}
