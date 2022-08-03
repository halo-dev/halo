package run.halo.app.theme;

import java.nio.file.Paths;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link ThemeContext}.
 *
 * @author guqing
 * @since 2.0.0
 */
class ThemeContextTest {

    @Test
    void constructorBuilderTest() throws JSONException {
        ThemeContext testTheme = ThemeContext.builder()
            .name("testTheme")
            .path(Paths.get("/tmp/themes/testTheme"))
            .active(true)
            .build();
        String s = JsonUtils.objectToJson(testTheme);
        JSONAssert.assertEquals("""
                {
                    "name": "testTheme",
                    "path": "file:///tmp/themes/testTheme",
                    "active": true
                }
                """,
            s,
            false);
    }
}