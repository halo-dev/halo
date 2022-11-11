package run.halo.app.theme;

import java.nio.file.Path;
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
        var path = Path.of("/tmp/themes/testTheme");
        var testTheme = ThemeContext.builder()
            .name("testTheme")
            .path(path)
            .active(true)
            .build();
        var got = JsonUtils.objectToJson(testTheme);
        var expect = String.format("""
                {
                    "name": "testTheme",
                    "path": "%s",
                    "active": true
                }
                """, path.toUri());
        JSONAssert.assertEquals(expect, got, false);
    }
}