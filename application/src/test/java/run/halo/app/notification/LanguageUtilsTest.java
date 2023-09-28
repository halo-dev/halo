package run.halo.app.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LanguageUtils}.
 *
 * @author guqing
 * @since 2.9.0
 */
class LanguageUtilsTest {

    @Test
    void computeLangFromLocale() {
        List<String> languages = LanguageUtils.computeLangFromLocale(Locale.CHINA);
        assertThat(languages).isEqualTo(List.of("default", "zh", "zh_CN"));

        languages = LanguageUtils.computeLangFromLocale(Locale.CHINESE);
        assertThat(languages).isEqualTo(List.of("default", "zh"));

        languages = LanguageUtils.computeLangFromLocale(Locale.TAIWAN);
        assertThat(languages).isEqualTo(List.of("default", "zh", "zh_TW"));

        languages = LanguageUtils.computeLangFromLocale(Locale.ENGLISH);
        assertThat(languages).isEqualTo(List.of("default", "en"));

        languages = LanguageUtils.computeLangFromLocale(Locale.US);
        assertThat(languages).isEqualTo(List.of("default", "en", "en_US"));

        languages =
            LanguageUtils.computeLangFromLocale(Locale.forLanguageTag("en-US-x-lvariant-POSIX"));
        assertThat(languages).isEqualTo(List.of("default", "en", "en_US", "en_US-POSIX"));
    }

    @Test
    void computeLangFromLocaleWhenLanguageIsEmpty() {
        assertThatThrownBy(() -> {
            LanguageUtils.computeLangFromLocale(Locale.forLanguageTag(""));
        }).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Locale \"\" cannot be used as it does not specify a language.");
    }
}
