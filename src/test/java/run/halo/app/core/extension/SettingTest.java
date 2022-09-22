package run.halo.app.core.extension;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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
        List<Unstructured> unstructureds =
            new YamlUnstructuredLoader(new InMemoryResource(settingYaml)).load();
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