package run.halo.app.extension;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.util.Map;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link Secret}.
 *
 * @author guqing
 * @since 2.4.0
 */
class SecretTest {

    @Test
    void serialize() throws JSONException {
        Secret secret = new Secret();
        secret.setMetadata(new Metadata());
        secret.getMetadata().setName("test-secret");
        secret.setType(Secret.SECRET_TYPE_OPAQUE);
        secret.setData(Map.of("password", "admin".getBytes()));
        String s = JsonUtils.objectToJson(secret);
        JSONAssert.assertEquals(testJsonString(), s, true);
    }

    @Test
    void deserialize() {
        String s = testJsonString();
        Secret secret = JsonUtils.jsonToObject(s, Secret.class);
        assertThat(secret).isNotNull();
        assertThat(secret.getMetadata().getName()).isEqualTo("test-secret");
        assertThat(secret.getType()).isEqualTo(Secret.SECRET_TYPE_OPAQUE);
        assertThat(secret.getData()).containsEntry("password", "admin".getBytes());
    }

    @Test
    void deserializeWithUnstructured() throws JsonProcessingException {
        Secret secret = Unstructured.OBJECT_MAPPER.readValue(testJsonString(), Secret.class);
        assertThat(secret.getMetadata().getName()).isEqualTo("test-secret");
        assertThat(secret.getType()).isEqualTo(Secret.SECRET_TYPE_OPAQUE);
        assertThat(secret.getData()).containsEntry("password", "admin".getBytes());
    }

    @Test
    void deserializeYamlWithStringData() throws JsonProcessingException {
        String s = """
            apiVersion: v1alpha1
            kind: Secret
            metadata:
              name: secret-basic-auth
            type: halo.run/basic-auth
            stringData:
              username: admin
              password: t0p-Secret
            """;
        Secret secret = new YAMLMapper().readValue(s, Secret.class);
        assertThat(secret.getMetadata().getName()).isEqualTo("secret-basic-auth");
        assertThat(secret.getType()).isEqualTo("halo.run/basic-auth");
        assertThat(secret.getStringData()).containsEntry("username", "admin");
        assertThat(secret.getStringData()).containsEntry("password", "t0p-Secret");
    }

    private String testJsonString() {
        return """
            {
                "apiVersion": "v1alpha1",
                "kind": "Secret",
                "metadata": {
                    "name": "test-secret"
                },
                "type": "Opaque",
                "data": {
                    "password": "YWRtaW4="
                }
            }
            """;
    }
}
