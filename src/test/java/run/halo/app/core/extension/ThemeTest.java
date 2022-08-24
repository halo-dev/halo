package run.halo.app.core.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link Theme}.
 *
 * @author guqing
 * @since 2.0.0
 */
class ThemeTest {

    @Test
    void constructor() throws JSONException {
        Theme theme = new Theme();
        Metadata metadata = new Metadata();
        metadata.setName("test-theme");
        theme.setMetadata(metadata);


        Theme.ThemeSpec themeSpec = new Theme.ThemeSpec();
        theme.setSpec(themeSpec);
        themeSpec.setDisplayName("test-theme");

        Theme.Author author = new Theme.Author();
        author.setName("test-author");
        author.setWebsite("https://test.com");
        themeSpec.setAuthor(author);

        themeSpec.setRepo("https://test.com");
        themeSpec.setLogo("https://test.com");
        themeSpec.setWebsite("https://test.com");
        themeSpec.setDescription("test-description");
        themeSpec.setConfigMapName("test-config-map");
        themeSpec.setSettingName("test-setting");

        themeSpec.setVersion(null);
        themeSpec.setRequire(null);
        JSONAssert.assertEquals("""
                {
                    "spec": {
                        "displayName": "test-theme",
                        "author": {
                            "name": "test-author",
                            "website": "https://test.com"
                        },
                        "description": "test-description",
                        "logo": "https://test.com",
                        "website": "https://test.com",
                        "repo": "https://test.com",
                        "version": "*",
                        "require": "*",
                        "settingName": "test-setting",
                        "configMapName": "test-config-map"
                    },
                    "apiVersion": "theme.halo.run/v1alpha1",
                    "kind": "Theme",
                    "metadata": {
                        "name": "test-theme"
                    }
                }
                """,
            JsonUtils.objectToJson(theme),
            true);

        themeSpec.setVersion("1.0.0");
        themeSpec.setRequire("2.0.0");
        assertThat(themeSpec.getVersion()).isEqualTo("1.0.0");
        assertThat(themeSpec.getRequire()).isEqualTo("2.0.0");
    }
}