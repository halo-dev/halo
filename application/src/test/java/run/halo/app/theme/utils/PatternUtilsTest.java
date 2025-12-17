package run.halo.app.theme.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import run.halo.app.infra.SystemSetting.ThemeRouteRules;

class PatternUtilsTest {

    @ParameterizedTest
    @CsvSource({
        "test, /test",
        "/test/, /test",
        "/test, /test",
        "test/, /test",
        "path/to/resource/, /path/to/resource"
    })
    void normalizePatternTest(String pattern, String expected) {
        assertEquals(expected, PatternUtils.normalizePattern(pattern));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        " ",
        "/",
        " /",
        "/ "
    })
    void shouldThrowExceptionWhen(String pattern) {
        assertThrows(IllegalArgumentException.class, () -> PatternUtils.normalizePattern(pattern));
    }

    @ParameterizedTest
    @CsvSource({
        "/posts/{slug}, /archives, /categories, /posts/{slug}",
        "/archives/{slug}, /blog/archives, /categories, /blog/archives/{slug}",
        "/categories/{slug}, /archives, /blog/categories, /blog/categories/{slug}",
        "archives/{slug}, blog/archives, /categories, /blog/archives/{slug}",
        "categories/{slug}, /archives, blog/categories, /blog/categories/{slug}",
        """
            /archives/{year}/{month}/{slug}/, /blog/archives/, /categories, \
            /blog/archives/{year}/{month}/{slug}""",
        """
            /categories/{category}/{slug}/, /archives, /blog/categories/, \
            /blog/categories/{category}/{slug}""",
        """
            /archives/categories/{slug}, /blog/archives, /blog/categories, \
            /blog/archives/categories/{slug}""",
    })
    void normalizePostPatternTest(
        String postPattern, String archivesPattern, String categoriesPattern, String expected
    ) {
        var rules = new ThemeRouteRules();
        rules.setPost(postPattern);
        rules.setArchives(archivesPattern);
        rules.setCategories(categoriesPattern);
        var result = PatternUtils.normalizePostPattern(rules);
        assertEquals(expected, result);
    }


}