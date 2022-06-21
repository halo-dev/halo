package run.halo.app.security.authorization;

import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.Role.PolicyRule;

/**
 * @author guqing
 * @since 2.0.0
 */
public class RbacRequestEvaluation {
    interface WildCard {
        String APIGroupAll = "*";
        String ResourceAll = "*";
        String VerbAll = "*";
        String NonResourceAll = "*";
    }

    public boolean rulesAllow(Attributes requestAttributes, List<Role.PolicyRule> rules) {
        for (Role.PolicyRule rule : rules) {
            if (ruleAllows(requestAttributes, rule)) {
                return true;
            }
        }
        return false;
    }

    protected boolean ruleAllows(Attributes requestAttributes, Role.PolicyRule rule) {
        if (requestAttributes.isResourceRequest()) {
            String combinedResource = requestAttributes.getResource();
            if (StringUtils.isNotBlank(requestAttributes.getSubresource())) {
                combinedResource =
                    requestAttributes.getResource() + "/" + requestAttributes.getSubresource();
            }

            return verbMatches(rule, requestAttributes.getVerb())
                && apiGroupMatches(rule, requestAttributes.getApiGroup())
                && resourceMatches(rule, combinedResource, requestAttributes.getSubresource())
                && resourceNameMatches(rule, requestAttributes.getName())
                && pluginNameMatches(rule, requestAttributes.pluginName());
        }
        return verbMatches(rule, requestAttributes.getVerb())
            && nonResourceURLMatches(rule, requestAttributes.getPath());
    }

    protected boolean verbMatches(Role.PolicyRule rule, String requestedVerb) {
        for (String ruleVerb : rule.getVerbs()) {
            if (Objects.equals(ruleVerb, WildCard.VerbAll)) {
                return true;
            }
            if (Objects.equals(ruleVerb, requestedVerb)) {
                return true;
            }
        }
        return false;
    }

    protected boolean apiGroupMatches(Role.PolicyRule rule, String requestedGroup) {
        for (String ruleGroup : rule.getApiGroups()) {
            if (Objects.equals(ruleGroup, WildCard.APIGroupAll)) {
                return true;
            }
            if (Objects.equals(ruleGroup, requestedGroup)) {
                return true;
            }
        }
        return false;
    }

    protected boolean resourceMatches(Role.PolicyRule rule, String combinedRequestedResource,
                                      String requestedSubresource) {
        for (String ruleResource : rule.getResources()) {
            // if everything is allowed, we match
            if (Objects.equals(ruleResource, WildCard.ResourceAll)) {
                return true;
            }
            // if we have an exact match, we match
            if (Objects.equals(ruleResource, combinedRequestedResource)) {
                return true;
            }

            // We can also match a */subresource.
            // if there isn't a subresource, then continue
            if (StringUtils.isBlank(requestedSubresource)) {
                continue;
            }
            // if the rule isn't in the format */subresource, then we don't match, continue
            if (StringUtils.length(ruleResource) == StringUtils.length(requestedSubresource) + 2
                && StringUtils.startsWith(ruleResource, "*/")
                && StringUtils.startsWith(ruleResource, requestedSubresource)) {
                return true;
            }
        }
        return false;
    }

    protected boolean resourceNameMatches(Role.PolicyRule rule, String requestedName) {
        if (ArrayUtils.isEmpty(rule.getResourceNames())) {
            return true;
        }
        for (String ruleName : rule.getResourceNames()) {
            if (Objects.equals(ruleName, requestedName)) {
                return true;
            }
        }
        return false;
    }

    protected boolean nonResourceURLMatches(Role.PolicyRule rule, String requestedURL) {
        for (String ruleURL : rule.getNonResourceURLs()) {
            if (Objects.equals(ruleURL, WildCard.NonResourceAll)) {
                return true;
            }
            if (Objects.equals(ruleURL, requestedURL)) {
                return true;
            }
            if (StringUtils.startsWith(ruleURL, WildCard.NonResourceAll)
                && StringUtils.startsWith(requestedURL,
                StringUtils.stripEnd(ruleURL, WildCard.NonResourceAll))) {
                return true;
            }
        }
        return false;
    }

    protected boolean pluginNameMatches(PolicyRule rule, String pluginName) {
        return StringUtils.equals(rule.getPluginName(), pluginName);
    }
}
