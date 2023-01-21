package run.halo.app.core.extension;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.security.util.InMemoryResource;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.infra.utils.YamlUnstructuredLoader;

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
        themeSpec.setRequires(null);
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
                        "requires": "*",
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
        themeSpec.setRequires("2.0.0");
        assertThat(themeSpec.getVersion()).isEqualTo("1.0.0");
        assertThat(themeSpec.getRequires()).isEqualTo("2.0.0");
    }

    @Test
    void themeCustomTemplate() throws JSONException {
        String themeYaml = """
            apiVersion: theme.halo.run/v1alpha1
            kind: Theme
            metadata:
              name: guqing-higan
            spec:
              displayName: higan
              customTemplates:
                post:
                  - name: post-template-1
                    description: description for post-template-1
                    screenshot: foo.png
                    file: post_template_1.html
                  - name: post-template-2
                    description: description for post-template-2
                    screenshot: bar.png
                    file: post_template_2.html
                category:
                  - name: category-template-1
                    description: description for category-template-1
                    screenshot: foo.png
                    file: category_template_1.html
                page:
                  - name: page-template-1
                    description: description for page-template-1
                    screenshot: foo.png
                    file: page_template_1.html
            """;
        List<Unstructured> unstructuredList =
            new YamlUnstructuredLoader(new InMemoryResource(themeYaml)).load();
        assertThat(unstructuredList).hasSize(1);
        Theme theme = Unstructured.OBJECT_MAPPER.convertValue(unstructuredList.get(0), Theme.class);
        assertThat(theme).isNotNull();
        JSONAssert.assertEquals("""
                {
                    "post": [
                        {
                            "name": "post-template-1",
                            "description": "description for post-template-1",
                            "screenshot": "foo.png",
                            "file": "post_template_1.html"
                        },
                        {
                            "name": "post-template-2",
                            "description": "description for post-template-2",
                            "screenshot": "bar.png",
                            "file": "post_template_2.html"
                        }
                    ],
                    "category": [
                        {
                            "name": "category-template-1",
                            "description": "description for category-template-1",
                            "screenshot": "foo.png",
                            "file": "category_template_1.html"
                        }],
                    "page": [
                        {
                            "name": "page-template-1",
                            "description": "description for page-template-1",
                            "screenshot": "foo.png",
                            "file": "page_template_1.html"
                        }]
                }
                """,
            JsonUtils.objectToJson(theme.getSpec().getCustomTemplates()),
            true);
    }
}