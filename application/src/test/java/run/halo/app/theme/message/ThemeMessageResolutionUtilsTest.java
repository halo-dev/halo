package run.halo.app.theme.message;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;
import run.halo.app.theme.ThemeContext;

/**
 * @author guqing
 * @since 2.0.0
 */
class ThemeMessageResolutionUtilsTest {
    private URL defaultThemeUrl;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        defaultThemeUrl = ResourceUtils.getURL("classpath:themes/default");
    }

    @Test
    void resolveMessagesForTemplateForDefault() throws URISyntaxException {
        Map<String, String> properties =
            ThemeMessageResolutionUtils.resolveMessagesForTemplate(Locale.CHINESE, getTheme());
        assertThat(properties).isEqualTo(Map.of("index.welcome", "欢迎来到首页",
            "title", "来自 i18n/zh.properties 的标题"));
    }

    @Test
    void resolveMessagesForTemplateForEnglish() throws URISyntaxException {
        Map<String, String> properties =
            ThemeMessageResolutionUtils.resolveMessagesForTemplate(Locale.ENGLISH, getTheme());
        assertThat(properties).isEqualTo(Map.of("index.welcome", "Welcome to the index",
            "title", "这是来自 i18n/default.properties 的标题"));
    }

    ThemeContext getTheme() throws URISyntaxException {
        return ThemeContext.builder()
            .name("default")
            .path(Path.of(defaultThemeUrl.toURI()))
            .active(true)
            .build();
    }
}