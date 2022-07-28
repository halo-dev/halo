package run.halo.app.theme;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ThemeLinkBuilder}.
 *
 * @author guqing
 * @since 2.0.0
 */
class ThemeLinkBuilderTest {
    private ThemeLinkBuilder themeLinkBuilder;

    @BeforeEach
    void setUp() {
        themeLinkBuilder = new ThemeLinkBuilder();
    }

    @Test
    void processTemplateLinkWithNoActive() {
        populateThemeContext(false);

        String link = "/post";
        String processed = themeLinkBuilder.processLink(null, link);
        assertThat(processed).isEqualTo("/post?theme=test-theme");

        processed = themeLinkBuilder.processLink(null, "/post?foo=bar");
        assertThat(processed).isEqualTo("/post?foo=bar&theme=test-theme");
    }

    @Test
    void processTemplateLinkWithActive() {
        populateThemeContext(true);

        String link = "/post";
        String processed = themeLinkBuilder.processLink(null, link);
        assertThat(processed).isEqualTo("/post");
    }

    @Test
    void processAssetsLink() {
        // activated theme
        populateThemeContext(true);

        String link = "/assets/css/style.css";
        String processed = themeLinkBuilder.processLink(null, link);
        assertThat(processed).isEqualTo("/themes/test-theme/assets/css/style.css");

        // preview theme
        populateThemeContext(false);
        link = "/assets/js/main.js";
        processed = themeLinkBuilder.processLink(null, link);
        assertThat(processed).isEqualTo("/themes/test-theme/assets/js/main.js");
    }

    @Test
    void processNullLink() {
        populateThemeContext(false);

        String link = null;
        String processed = themeLinkBuilder.processLink(null, link);
        assertThat(processed).isEqualTo(null);

        // empty link
        link = "";
        processed = themeLinkBuilder.processLink(null, link);
        assertThat(processed).isEqualTo("/?theme=test-theme");
    }

    @Test
    void processAbsoluteLink() {
        populateThemeContext(false);
        String link = "https://github.com/halo-dev";
        String processed = themeLinkBuilder.processLink(null, link);
        assertThat(processed).isEqualTo(link);

        link = "http://example.com";
        processed = themeLinkBuilder.processLink(null, link);
        assertThat(processed).isEqualTo(link);

        link = "//example.com";
        processed = themeLinkBuilder.processLink(null, link);
        assertThat(processed).isEqualTo(link);
    }

    private void populateThemeContext(boolean isActive) {
        ThemeContext themeContext = ThemeContext.builder()
            .themeName("test-theme")
            .path(Path.of("/themes/test-theme"))
            .isActive(isActive).build();
        ThemeContextHolder.setThemeContext(themeContext);
    }
}