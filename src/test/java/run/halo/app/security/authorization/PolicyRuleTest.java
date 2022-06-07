package run.halo.app.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link PolicyRule}.
 *
 * @author guqing
 * @since 2.0.0
 */
class PolicyRuleTest {
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonUtils.DEFAULT_JSON_MAPPER;
    }

    @Test
    public void constructPolicyRule() throws JsonProcessingException {
        PolicyRule policyRule = new PolicyRule(null, null, null, null, null);
        assertThat(policyRule).isNotNull();
        JsonNode policyRuleJson = objectMapper.valueToTree(policyRule);
        assertThat(policyRuleJson).isEqualTo(objectMapper.readTree("""
            {"apiGroups":[],"resources":[],"resourceNames":[],"nonResourceURLs":[],"verbs":[]}
            """));

        PolicyRule policyByBuilder = new PolicyRule.Builder().build();
        JsonNode policyByBuilderJson = objectMapper.valueToTree(policyByBuilder);
        assertThat(policyByBuilderJson).isEqualTo(objectMapper.readTree("""
            {"apiGroups":[],"resources":[],"resourceNames":[],"nonResourceURLs":[],"verbs":[]}
             """));

        PolicyRule policyNonNull = new PolicyRule.Builder()
            .apiGroups("group")
            .resources("resource-1", "resource-2")
            .resourceNames("resourceName")
            .nonResourceURLs("non resource url")
            .verbs("verbs")
            .build();

        JsonNode expected = objectMapper.readTree("""
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
        JsonNode policyNonNullJson = objectMapper.valueToTree(policyNonNull);
        assertThat(policyNonNullJson).isEqualTo(expected);
    }
}