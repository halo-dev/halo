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
        assertThat(properties).hasSize(1);
        assertThat(properties).containsEntry("index.welcome", "欢迎来到首页");
    }

    @Test
    void resolveMessagesForTemplateForEnglish() throws URISyntaxException {
        Map<String, String> properties =
            ThemeMessageResolutionUtils.resolveMessagesForTemplate(Locale.ENGLISH, getTheme());
        assertThat(properties).hasSize(1);
        assertThat(properties).containsEntry("index.welcome", "Welcome to the index");
    }

    @Test
    void messageFormat() {
        String s =
            ThemeMessageResolutionUtils.formatMessage(Locale.ENGLISH, "Welcome {0} to the index",
                new Object[] {"Halo"});
        assertThat(s).isEqualTo("Welcome Halo to the index");
    }

    ThemeContext getTheme() throws URISyntaxException {
        return ThemeContext.builder()
            .name("default")
            .path(Path.of(defaultThemeUrl.toURI()))
            .active(true)
            .build();
    }
}