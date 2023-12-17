package run.halo.app.theme.router;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PageUrlUtils}.
 *
 * @author guqing
 * @since 2.0.0
 */
class PageUrlUtilsTest {
    static String s = "/tags";
    static String s1 = "/tags/page/1";
    static String s2 = "/tags/page/2";
    static String s3 = "/tags/y/m/page/2";
    static String s4 = "/tags/y/m";
    static String s5 = "/tags/y/m/page/3";

    @Test
    void nextPageUrl() {
        long totalPage = 10;
        assertThat(PageUrlUtils.nextPageUrl(s, totalPage))
            .isEqualTo("/tags/page/2");
        assertThat(PageUrlUtils.nextPageUrl(s2, totalPage))
            .isEqualTo("/tags/page/3");
        assertThat(PageUrlUtils.nextPageUrl(s3, totalPage))
            .isEqualTo("/tags/y/m/page/3");
        assertThat(PageUrlUtils.nextPageUrl(s4, totalPage))
            .isEqualTo("/tags/y/m/page/2");
        assertThat(PageUrlUtils.nextPageUrl(s5, totalPage))
            .isEqualTo("/tags/y/m/page/4");

        // The number of pages does not exceed the total number of pages
        totalPage = 1;
        assertThat(PageUrlUtils.nextPageUrl("/tags/page/1", totalPage))
            .isEqualTo("/tags/page/1");

        totalPage = 0;
        assertThat(PageUrlUtils.nextPageUrl("/tags", totalPage))
            .isEqualTo("/tags/page/1");
    }

    @Test
    void prevPageUrl() {
        assertThat(PageUrlUtils.prevPageUrl(s))
            .isEqualTo("/tags");
        assertThat(PageUrlUtils.prevPageUrl(s1))
            .isEqualTo("/tags");
        assertThat(PageUrlUtils.prevPageUrl(s2))
            .isEqualTo("/tags");
        assertThat(PageUrlUtils.prevPageUrl(s3))
            .isEqualTo("/tags/y/m");
        assertThat(PageUrlUtils.prevPageUrl(s4))
            .isEqualTo("/tags/y/m");
        assertThat(PageUrlUtils.prevPageUrl(s5))
            .isEqualTo("/tags/y/m/page/2");

        assertThat(PageUrlUtils.prevPageUrl("/page/2"))
            .isEqualTo("/");
    }
}