package run.halo.app.core.extension.theme;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
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

    @Test
    void mergePatch() throws JSONException {
        Map<String, String> defaultValue =
            Map.of("comment", "{\"enable\":true,\"requireReviewForNew\":true}",
                "basic", "{\"title\":\"guqing's blog\"}",
                "authProvider", "{\"github\":{\"clientId\":\"fake-client-id\"}}");
        Map<String, String> modified = Map.of("comment",
            "{\"enable\":true,\"requireReviewForNew\":true,\"systemUserOnly\":false}",
            "basic", "{\"title\":\"guqing's blog\", \"subtitle\": \"fake-sub-title\"}");

        Map<String, String> result = SettingUtils.mergePatch(modified, defaultValue);
        Map<String, String> excepted = Map.of("comment",
            "{\"enable\":true,\"requireReviewForNew\":true,\"systemUserOnly\":false}",
            "basic", "{\"title\":\"guqing's blog\",\"subtitle\":\"fake-sub-title\"}",
            "authProvider", "{\"github\":{\"clientId\":\"fake-client-id\"}}");
        JSONAssert.assertEquals(JsonUtils.objectToJson(excepted), JsonUtils.objectToJson(result),
            true);
    }

    @Test
    void mergePatchWithMoreType() throws JSONException {
        Map<String, String> defaultValue = Map.of(
            "array", "[1,2,3]",
            "number", "1",
            "boolean", "false",
            "string", "new-default-string-value",
            "object", "{\"name\":\"guqing\"}"
        );
        Map<String, String> modified = Map.of(
            "stringArray", "[\"hello\", \"world\"]",
            "boolean", "true",
            "string", "hello",
            "object", "{\"name\":\"guqing\", \"age\": 18}"
        );
        Map<String, String> result = SettingUtils.mergePatch(modified, defaultValue);
        Map<String, String> excepted = Map.of(
            "array", "[1,2,3]",
            "number", "1",
            "boolean", "true",
            "string", "hello",
            "object", "{\"name\":\"guqing\",\"age\":18}",
            "stringArray", "[\"hello\",\"world\"]"
        );
        JSONAssert.assertEquals(JsonUtils.objectToJson(excepted), JsonUtils.objectToJson(result),
            true);
    }

    @Test
    void isJson() {
        assertThat(SettingUtils.isJson("[1,2,3]")).isTrue();
        assertThat(SettingUtils.isJson("[\"hello\"]")).isTrue();
        assertThat(SettingUtils.isJson("{\"name\":\"guqing\",\"age\":18}")).isTrue();
        assertThat(SettingUtils.isJson("{ \"flag\":true }")).isTrue();
        assertThat(SettingUtils.isJson("""
            [
                { "K1": "value-1", "K2":"value1-2" }
            ]
            """)).isTrue();
        assertThat(SettingUtils.isJson("""
            {
                "sites": [{ "name":"halo" , "url":"halo.run" }]
            }
            """)).isTrue();
        assertThat(SettingUtils.isJson("{\"name\":\"guqing\"")).isFalse();
        assertThat(SettingUtils.isJson("hello")).isFalse();
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