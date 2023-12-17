package run.halo.app.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.ConfigMap;
import run.halo.app.infra.SystemSetting.Comment;
import run.halo.app.infra.SystemSetting.ExtensionPointEnabled;
import run.halo.app.infra.utils.JsonUtils;

class SystemSettingTest {

    @Nested
    class ExtensionPointEnabledTest {

        @Test
        void deserializeTest() {
            var json = """
                    {
                      "run.halo.app.search.post.PostSearchService": [
                        "run.halo.app.search.post.LucenePostSearchService"
                      ]
                    }
                """;

            var enabled = JsonUtils.jsonToObject(json, ExtensionPointEnabled.class);
            assertTrue(enabled.containsKey("run.halo.app.search.post.PostSearchService"));
        }
    }

    @Test
    void shouldGetConfigFromJson() {
        var configMap = new ConfigMap();
        configMap.putDataItem("comment", """
            {"enable": true}
            """);
        var comment = SystemSetting.get(configMap, Comment.GROUP, Comment.class);
        assertTrue(comment.getEnable());
    }

    @Test
    void shouldGetNullIfKeyNotExist() {
        var configMap = new ConfigMap();
        configMap.setData(new HashMap<>());
        String fake = SystemSetting.get(configMap, "fake-key", String.class);
        assertNull(fake);
    }

    @Test
    void shouldGetConfigViaConversionService() {
        var configMap = new ConfigMap();
        configMap.putDataItem("int", "100");
        var integer = SystemSetting.get(configMap, "int", Integer.class);
        assertEquals(100, integer);
    }
}