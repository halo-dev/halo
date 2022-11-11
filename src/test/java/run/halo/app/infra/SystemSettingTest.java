package run.halo.app.infra;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

}