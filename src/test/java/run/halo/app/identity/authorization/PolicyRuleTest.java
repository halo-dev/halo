package run.halo.app.identity.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link PolicyRule}.
 *
 * @author guqing
 * @since 2.0.0
 */
class PolicyRuleTest {

    @Test
    public void constructPolicyRule() throws JsonProcessingException {
        PolicyRule policyRule = new PolicyRule(null, null, null, null, null);
        assertThat(policyRule).isNotNull();
        assertThat(JsonUtils.objectToJson(policyRule)).isEqualToIgnoringWhitespace("""
            {"apiGroups":[],"resources":[],"resourceNames":[],"nonResourceURLs":[],"verbs":[]}
             """);

        PolicyRule policyByBuilder = new PolicyRule.Builder().build();
        assertThat(JsonUtils.objectToJson(policyByBuilder)).isEqualToIgnoringWhitespace("""
            {"apiGroups":[],"resources":[],"resourceNames":[],"nonResourceURLs":[],"verbs":[]}
             """);

        PolicyRule policyNonNull = new PolicyRule.Builder()
            .apiGroups("group")
            .resources("resource-1", "resource-2")
            .resourceNames("resourceName")
            .nonResourceURLs("non resource url")
            .verbs("verbs")
            .build();

        assertThat(JsonUtils.objectToJson(policyNonNull)).isEqualToIgnoringWhitespace("""
            {
                "apiGroups": [
                    "group"
                ],
                "resources": [
                    "resource-1",
                    "resource-2"
                ],
                "resourceNames": [
                    "resourceName"
                ],
                "nonResourceURLs": [
                    "non resource url"
                ],
                "verbs": [
                    "verbs"
                ]
            }
            """);
    }
}