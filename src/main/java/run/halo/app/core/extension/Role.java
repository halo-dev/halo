package run.halo.app.core.extension;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * @author guqing
 * @since 2.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@GVK(group = "",
    version = "v1alpha1",
    kind = "Role",
    plural = "roles",
    singular = "role")
public class Role extends AbstractExtension {

    @Schema(minLength = 1)
    List<PolicyRule> rules;

    /**
     * PolicyRule holds information that describes a policy rule, but does not contain information
     * about whom the rule applies to or which namespace the rule applies to.
     *
     * @author guqing
     * @since 2.0.0
     */
    @Getter
    public static class PolicyRule {
        /**
         * APIGroups is the name of the APIGroup that contains the resources.
         * If multiple API groups are specified, any action requested against one of the enumerated
         * resources in any API group will be allowed.
         */
        final String[] apiGroups;

        /**
         * Resources is a list of resources this rule applies to.  '*' represents all resources in
         * the specified apiGroups.
         * '*&#47;foo' represents the subresource 'foo' for all resources in the specified
         * apiGroups.
         */
        final String[] resources;

        /**
         * ResourceNames is an optional white list of names that the rule applies to.  An empty set
         * means that everything is allowed.
         */
        final String[] resourceNames;

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
        final String[] nonResourceURLs;

        /**
         * about who the rule applies to or which namespace the rule applies to.
         */
        final String[] verbs;

        /**
         * If the plugin name exists, it means that the API is provided by the plugin.
         */
        final String pluginName;

        public PolicyRule() {
            this(null, null, null, null, null, null);
        }

        public PolicyRule(String pluginName, String[] apiGroups, String[] resources,
            String[] resourceNames,
            String[] nonResourceURLs, String[] verbs) {
            this.pluginName = StringUtils.defaultString(pluginName);
            this.apiGroups = nullElseEmpty(apiGroups);
            this.resources = nullElseEmpty(resources);
            this.resourceNames = nullElseEmpty(resourceNames);
            this.nonResourceURLs = nullElseEmpty(nonResourceURLs);
            this.verbs = nullElseEmpty(verbs);
        }

        String[] nullElseEmpty(String... items) {
            if (items == null) {
                return new String[] {};
            }
            return items;
        }

        public static class Builder {
            String[] apiGroups;

            String[] resources;

            String[] resourceNames;

            String[] nonResourceURLs;

            String[] verbs;

            String pluginName;

            public Builder pluginName(String pluginName) {
                this.pluginName = pluginName;
                return this;
            }

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

            public PolicyRule build() {
                return new PolicyRule(pluginName, apiGroups, resources, resourceNames,
                    nonResourceURLs,
                    verbs);
            }
        }
    }
}
