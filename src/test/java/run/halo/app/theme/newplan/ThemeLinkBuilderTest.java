package run.halo.app.theme.newplan;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ThemeLinkBuilder}.
 *
 * @author guqing
 * @since 2.0.0
 */
class ThemeLinkBuilderTest {
    private ThemeLinkBuilder themeLinkBuilder;

    @Test
    void processTemplateLinkWithNoActive() {
        themeLinkBuilder = new ThemeLinkBuilder(getTheme(false));

        String link = "/post";
        String processed = themeLinkBuilder.processLink(null, link);
        assertThat(processed).isEqualTo("/post?theme=test-theme");

        processed = themeLinkBuilder.processLink(null, "/post?foo=bar");
        assertThat(processed).isEqualTo("/post?foo=bar&theme=test-theme");
    }

    @Test
    void processTemplateLinkWithActive() {
        themeLinkBuilder = new ThemeLinkBuilder(getTheme(true));

        String link = "/post";
        String processed = themeLinkBuilder.processLink(null, link);
        assertThat(processed).isEqualTo("/post");
    }

    @Test
    void processAssetsLink() {
        // activated theme
        themeLinkBuilder = new ThemeLinkBuilder(getTheme(true));

        String link = "/assets/css/style.css";
        String processed = themeLinkBuilder.processLink(null, link);
        assertThat(processed).isEqualTo("/themes/test-theme/assets/css/style.css");

        // preview theme
        getTheme(false);
        link = "/assets/js/main.js";
        processed = themeLinkBuilder.processLink(null, link);
        assertThat(processed).isEqualTo("/themes/test-theme/assets/js/main.js");
    }

    @Test
    void processNullLink() {
        themeLinkBuilder = new ThemeLinkBuilder(getTheme(false));

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
        themeLinkBuilder = new ThemeLinkBuilder(getTheme(false));
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

    private ThemeContext getTheme(boolean isActive) {
        return ThemeContext.builder()
            .name("test-theme")
            .path("/themes/test-theme")
            .active(isActive)
            .build();
    }
}