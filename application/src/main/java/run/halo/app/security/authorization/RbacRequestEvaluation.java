package run.halo.app.security.authorization;

import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import run.halo.app.core.extension.Role;

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
                && resourceNameMatches(rule,
                combineResourceName(requestAttributes.getName(), requestAttributes.getSubName()));
        }
        return verbMatches(rule, requestAttributes.getVerb())
            && nonResourceURLMatches(rule, requestAttributes.getPath());
    }

    private String combineResourceName(String name, String subName) {
        if (StringUtils.isBlank(name)) {
            return subName;
        }
        if (StringUtils.isBlank(subName)) {
            return name;
        }
        return name + "/" + subName;
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
        String[] requestedNameParts = ArrayUtils.nullToEmpty(StringUtils.split(requestedName, "/"));
        for (String ruleName : rule.getResourceNames()) {
            String[] patternParts = StringUtils.split(ruleName, "/");

            for (int i = 0; i < patternParts.length; i++) {
                String patternPart = patternParts[i];
                String textPart = StringUtils.EMPTY;
                if (requestedNameParts.length > i) {
                    textPart = requestedNameParts[i];
                }

                if (!matchPart(patternPart, textPart)) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    private static boolean matchPart(String patternPart, String textPart) {
        if (patternPart.equals("*")) {
            return true;
        } else if (patternPart.startsWith("*")) {
            return textPart.endsWith(patternPart.substring(1));
        } else if (patternPart.endsWith("*")) {
            return textPart.startsWith(patternPart.substring(0, patternPart.length() - 1));
        } else {
            return patternPart.equals(textPart);
        }
    }

    protected boolean nonResourceURLMatches(Role.PolicyRule rule, String requestedURL) {
        for (String ruleURL : rule.getNonResourceURLs()) {
            if (Objects.equals(ruleURL, WildCard.NonResourceAll)) {
                return true;
            }
            if (Objects.equals(ruleURL, requestedURL)) {
                return true;
            }
            if (StringUtils.endsWith(ruleURL, WildCard.NonResourceAll)
                && StringUtils.startsWith(requestedURL,
                StringUtils.stripEnd(ruleURL, WildCard.NonResourceAll))) {
                return true;
            }
        }
        return false;
    }
}
