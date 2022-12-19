package run.halo.app.core.extension.theme;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import run.halo.app.core.extension.Setting;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link SettingUtils}.
 *
 * @author guqing
 * @since 2.0.1
 */
class SettingUtilsTest {

    @Test
    void settingDefinedDefaultValueMap() throws JSONException {
        Setting setting = getFakeSetting();
        var map = SettingUtils.settingDefinedDefaultValueMap(setting);
        JSONAssert.assertEquals("""
                {
                    "sns": "{\\"email\\":\\"example@exmple.com\\"}"
                }
                """,
            JsonUtils.objectToJson(map),
            true);
    }

    private static Setting getFakeSetting() {
        String settingJson = """
            {
                "apiVersion": "v1alpha1",
                "kind": "Setting",
                "metadata": {
                    "name": "theme-default-setting"
                },
                "spec": {
                    "forms": [{
                        "formSchema": [
                            {
                                "$el": "h1",
                                "children": "Register"
                            },
                            {
                                "$formkit": "text",
                                "label": "Email",
                                "name": "email",
                                "value": "example@exmple.com"
                            },
                            {
                                "$formkit": "password",
                                "label": "Password",
                                "name": "password",
                                "validation": "required|length:5,16",
                                "value": null
                            }
                        ],
                        "group": "sns",
                        "label": "社交资料"
                    }]
                }
            }
            """;
        return JsonUtils.jsonToObject(settingJson, Setting.class);
    }
}