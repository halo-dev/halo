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
    void constructorTest() throws JSONException {
        ThemeContext testTheme =
            new ThemeContext("testTheme", Paths.get("/tmp/themes/testTheme"), false);
        String s = JsonUtils.objectToJson(testTheme);
        JSONAssert.assertEquals("""
            {
                "themeName": "testTheme",
                "path": "file:///tmp/themes/testTheme",
                "active": false
            }
            """,
            s,
            false);
    }

    @Test
    void constructorBuilderTest() throws JSONException {
        ThemeContext testTheme = ThemeContext.builder()
            .themeName("testTheme")
            .path(Paths.get("/tmp/themes/testTheme"))
            .isActive(true)
            .build();
        String s = JsonUtils.objectToJson(testTheme);
        JSONAssert.assertEquals("""
            {
                "themeName": "testTheme",
                "path": "file:///tmp/themes/testTheme",
                "active": true
            }
            """,
            s,
            false);
    }
}