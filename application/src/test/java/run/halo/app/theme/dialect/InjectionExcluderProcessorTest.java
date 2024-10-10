package run.halo.app.theme.dialect;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link InjectionExcluderProcessor}.
 *
 * @author guqing
 * @since 2.20.0
 */
class InjectionExcluderProcessorTest {

    @Nested
    class PageInjectionExcluderTest {
        final InjectionExcluderProcessor.PageInjectionExcluder pageInjectionExcluder =
            new InjectionExcluderProcessor.PageInjectionExcluder();

        @Test
        void excludeTest() {
            var cases = new String[] {
                "login",
                "signup",
                "logout",
                "password-reset/email/reset",
                "error/404",
                "error/500",
                "challenges/totp"
            };

            for (String templateName : cases) {
                assertThat(pageInjectionExcluder.isExcluded(templateName)).isTrue();
            }
        }

        @Test
        void shouldNotExcludeTest() {
            var cases = new String[] {
                "index",
                "post",
                "page",
                "category",
                "tag",
                "archive",
                "search",
                "feed",
                "sitemap",
                "robots",
                "custom",
                "error",
                "login.html",
            };

            for (String templateName : cases) {
                assertThat(pageInjectionExcluder.isExcluded(templateName)).isFalse();
            }
        }
    }
}