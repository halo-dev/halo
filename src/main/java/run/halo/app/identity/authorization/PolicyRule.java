package run.halo.app.identity.authorization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PolicyRule holds information that describes a policy rule, but does not contain information
 * about who the rule applies to or which namespace the rule applies to.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PolicyRule {

    /**
     * APIGroups is the name of the APIGroup that contains the resources.
     * If multiple API groups are specified, any action requested against one of the enumerated
     * resources in any API group will be allowed.
     */
    String[] apiGroups;

    /**
     * Resources is a list of resources this rule applies to.  '*' represents all resources in
     * the specified apiGroups.
     * '*&#47;foo' represents the subresource 'foo' for all resources in the specified apiGroups.
     */
    String[] resources;

    /**
     * ResourceNames is an optional white list of names that the rule applies to.  An empty set
     * means that everything is allowed.
     */
    String[] resourceNames;

    /**
     * NonResourceURLs is a set of partial urls that a user should have access to.
     * *s are allowed, but only as the full, final step in the path
     * If an action is not a resource API request, then the URL is split on '/' and is checked
     * against the NonResourceURLs to look for a match.
     * Since non-resource URLs are not namespaced, this field is only applicable for
     * ClusterRoles referenced from a ClusterRoleBinding.
     * Rules can either apply to API resources (such as "pods" or "secrets") or non-resource
     * URL paths (such as "/api"),  but not both.
     */
    String[] nonResourceURLs;

    /**
     * about who the rule applies to or which namespace the rule applies to.
     */
    String[] verbs;

    public static class Builder {
        String[] apiGroups;
        String[] resources;
        String[] resourceNames;
        String[] nonResourceURLs;
        String[] verbs;

        public Builder apiGroups(String... apiGroups) {
            this.apiGroups = apiGroups;
            return this;
        }

        public Builder resources(String... resources) {
            this.resources = resources;
            return this;
        }

        public Builder resourceNames(String... resourceNames) {
            this.resourceNames = resourceNames;
            return this;
        }

        public Builder nonResourceURLs(String... nonResourceURLs) {
            this.nonResourceURLs = nonResourceURLs;
            return this;
        }

        public Builder verbs(String... verbs) {
            this.verbs = verbs;
            return this;
        }

        String[] nullElseEmpty(String... items) {
            if (items == null) {
                return new String[] {};
            }
            return items;
        }

        public PolicyRule build() {
            return new PolicyRule(
                nullElseEmpty(apiGroups),
                nullElseEmpty(resources),
                nullElseEmpty(resourceNames),
                nullElseEmpty(nonResourceURLs),
                nullElseEmpty(verbs)
            );
        }
    }
}
