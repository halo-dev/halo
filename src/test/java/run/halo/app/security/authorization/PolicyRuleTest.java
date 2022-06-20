package run.halo.app.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import run.halo.app.core.extension.Role;
import org.skyscreamer.jsonassert.JSONAssert;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link Role.PolicyRule}.
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
        Role.PolicyRule policyRule = new Role.PolicyRule(null, null, null, null, null);
        assertThat(policyRule).isNotNull();
        JSONAssert.assertEquals("""
            {
                "pluginName": "",
                "apiGroups": [],
                "resources": [],
                "resourceNames": [],
                "nonResourceURLs": [],
                "verbs": []
            }
            """,
            JsonUtils.objectToJson(policyRule),
            true);

        Role.PolicyRule policyByBuilder = new Role.PolicyRule.Builder().build();
        JSONAssert.assertEquals("""
            {
                "pluginName": "",
                "apiGroups": [],
                "resources": [],
                "resourceNames": [],
                "nonResourceURLs": [],
                "verbs": []
            }
            """,
            JsonUtils.objectToJson(policyByBuilder),
            true);

        Role.PolicyRule policyNonNull = new Role.PolicyRule.Builder()
            .pluginName("fakePluginName")
            .apiGroups("group")
            .resources("resource-1", "resource-2")
            .resourceNames("resourceName")
            .nonResourceURLs("non resource url")
            .verbs("verbs")
            .build();

        JsonNode expected = objectMapper.readTree("""
            {
                "pluginName": "fakePluginName",
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