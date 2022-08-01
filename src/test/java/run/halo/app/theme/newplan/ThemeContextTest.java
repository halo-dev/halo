package run.halo.app.theme.newplan;

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
            new ThemeContext("testTheme", "/tmp/themes/testTheme", false);
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
            .name("testTheme")
            .path("/tmp/themes/testTheme")
            .active(true)
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