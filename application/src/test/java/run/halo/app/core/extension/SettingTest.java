package run.halo.app.core.extension;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.security.util.InMemoryResource;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.infra.utils.YamlUnstructuredLoader;

/**
 * Tests for {@link Setting}.
 *
 * @author guqing
 * @since 2.0.0
 */
class SettingTest {

    @Test
    void setting() throws JSONException {
        String settingYaml = """
            apiVersion: v1alpha1
            kind: Setting
            metadata:
              name: setting-name
            spec:
              forms:
                - group: basic
                  label: 基本设置
                  formSchema:
                    - $el: h1
                      children: Register
                    - $formkit: text
                      help: This will be used for your account.
                      label: Email
                      name: email
                      validation: required|email
                - group: sns
                  label: 社交资料
                  formSchema:
                    - $formkit: text
                      help: This will be used for your theme.
                      label: color
                      name: color
                      validation: required
            """;
        var unstructureds = new YamlUnstructuredLoader(
            new InMemoryResource(settingYaml.getBytes(UTF_8), "In-memory setting YAML"))
            .load();
        assertThat(unstructureds).hasSize(1);
        Unstructured unstructured = unstructureds.get(0);

        Setting setting = Unstructured.OBJECT_MAPPER.convertValue(unstructured, Setting.class);
        assertThat(setting).isNotNull();
        JSONAssert.assertEquals("""
                {
                     "spec": {
                         "forms": [
                             {
                                 "group": "basic",
                                 "label": "基本设置",
                                 "formSchema": [
                                     {
                                         "$el": "h1",
                                         "children": "Register"
                                     },
                                     {
                                         "$formkit": "text",
                                         "help": "This will be used for your account.",
                                         "label": "Email",
                                         "name": "email",
                                         "validation": "required|email"
                                     }
                                 ]
                             },
                             {
                                 "group": "sns",
                                 "label": "社交资料",
                                 "formSchema": [
                                     {
                                         "$formkit": "text",
                                         "help": "This will be used for your theme.",
                                         "label": "color",
                                         "name": "color",
                                         "validation": "required"
                                     }
                                 ]
                             }
                         ]
                     },
                     "apiVersion": "v1alpha1",
                     "kind": "Setting",
                     "metadata": {
                         "name": "setting-name"
                     }
                }
                """,
            JsonUtils.objectToJson(setting), false);
    }
}