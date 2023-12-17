package run.halo.app.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.infra.ExternalUrlSupplier;

/**
 * Tests for {@link ThemeLinkBuilder}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class ThemeLinkBuilderTest {
    @Mock
    private ExternalUrlSupplier externalUrlSupplier;

    @BeforeEach
    void setUp() {
        // Mock external url supplier
        lenient().when(externalUrlSupplier.get()).thenReturn(URI.create(""));
    }

    @Test
    void processTemplateLinkWithNoActive() {
        ThemeLinkBuilder themeLinkBuilder =
            new ThemeLinkBuilder(getTheme(false), externalUrlSupplier);

        String link = "/post";
        String processed = themeLinkBuilder.processLink(null, link);
        assertThat(processed).isEqualTo("/post?preview-theme=test-theme");

        processed = themeLinkBuilder.processLink(null, "/post?foo=bar");
        assertThat(processed).isEqualTo("/post?foo=bar&preview-theme=test-theme");
    }

    @Test
    void processTemplateLinkWithActive() {
        ThemeLinkBuilder themeLinkBuilder =
            new ThemeLinkBuilder(getTheme(true), externalUrlSupplier);

        String link = "/post";
        String processed = themeLinkBuilder.processLink(null, link);
        assertThat(processed).isEqualTo("/post");
    }

    @Test
    void processAssetsLink() {
        // activated theme
        ThemeLinkBuilder themeLinkBuilder =
            new ThemeLinkBuilder(getTheme(true), externalUrlSupplier);

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
        ThemeLinkBuilder themeLinkBuilder =
            new ThemeLinkBuilder(getTheme(false), externalUrlSupplier);

        String link = null;
        String processed = themeLinkBuilder.processLink(null, link);
        assertThat(processed).isEqualTo(null);

        // empty link
        link = "";
        processed = themeLinkBuilder.processLink(null, link);
        assertThat(processed).isEqualTo("/?preview-theme=test-theme");
    }

    @Test
    void processAbsoluteLink() {
        ThemeLinkBuilder themeLinkBuilder =
            new ThemeLinkBuilder(getTheme(false), externalUrlSupplier);
        String link = "https://github.com/halo-dev";
        String processed = themeLinkBuilder.processLink(null, link);
        assertThat(processed).isEqualTo(link);

        link = "http://example.com";
        processed = themeLinkBuilder.processLink(null, link);
        assertThat(processed).isEqualTo(link);
    }

    @Test
    void linkInSite() throws URISyntaxException {
        URI uri = new URI("");
        // relative link is always in site
        assertThat(ThemeLinkBuilder.linkInSite(uri, "/post")).isTrue();

        // absolute link is not in site
        assertThat(ThemeLinkBuilder.linkInSite(uri, "https://example.com")).isFalse();

        uri = new URI("https://example.com");
        // link in externalUrl is in site link
        assertThat(ThemeLinkBuilder.linkInSite(uri, "http://example.com/hello/world")).isTrue();
        // scheme is different but authority is same
        assertThat(ThemeLinkBuilder.linkInSite(uri, "https://example.com/hello/world")).isTrue();

        // scheme is same and authority is different
        assertThat(ThemeLinkBuilder.linkInSite(uri, "http://halo.run/hello/world")).isFalse();
        // scheme is different and authority is different
        assertThat(ThemeLinkBuilder.linkInSite(uri, "https://halo.run/hello/world")).isFalse();

        // port is different
        uri = new URI("http://localhost:8090");
        assertThat(ThemeLinkBuilder.linkInSite(uri, "http://localhost:3000")).isFalse();
    }

    private ThemeContext getTheme(boolean isActive) {
        return ThemeContext.builder()
            .name("test-theme")
            .path(Paths.get("/themes/test-theme"))
            .active(isActive)
            .build();
    }
}