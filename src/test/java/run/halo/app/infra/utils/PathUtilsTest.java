package run.halo.app.infra.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PathUtils}.
 *
 * @author guqing
 * @since 2.0.0
 */
class PathUtilsTest {

    @Test
    void combinePath() {
        Map<String, String> combinePathCases = getCombinePathCases();
        combinePathCases.forEach((segments, expected) -> {
            String s = PathUtils.combinePath(segments.split(","));
            assertThat(s).isEqualTo(expected);
        });

        String s = PathUtils.combinePath("a", "", "c");
        assertThat(s).isEqualTo("/a/c");
    }

    private Map<String, String> getCombinePathCases() {
        Map<String, String> combinePathCases = new HashMap<>();
        combinePathCases.put("a,b,c", "/a/b/c");
        combinePathCases.put("/a,b,c", "/a/b/c");
        combinePathCases.put("/a,b/,c", "/a/b/c");
        combinePathCases.put("/a,/b/,c", "/a/b/c");
        return combinePathCases;
    }

    @Test
    void appendPathSeparatorIfMissing() {
        String s = PathUtils.appendPathSeparatorIfMissing("a");
        assertThat(s).isEqualTo("a/");

        s = PathUtils.appendPathSeparatorIfMissing("a/");
        assertThat(s).isEqualTo("a/");

        s = PathUtils.appendPathSeparatorIfMissing(null);
        assertThat(s).isEqualTo(null);
    }

    @Test
    void simplifyPathPattern() {
        assertThat(PathUtils.simplifyPathPattern("/a/b/c")).isEqualTo("/a/b/c");
        assertThat(PathUtils.simplifyPathPattern("/a/{b}/c")).isEqualTo("/a/{b}/c");
        assertThat(PathUtils.simplifyPathPattern("/a/{b}/*")).isEqualTo("/a/{b}/*");
        assertThat(PathUtils.simplifyPathPattern("/archives/{year:\\d{4}}/{month:\\d{2}}"))
            .isEqualTo("/archives/{year}/{month}");
        assertThat(PathUtils.simplifyPathPattern("/archives/{year:\\d{4}}/{slug}"))
            .isEqualTo("/archives/{year}/{slug}");
        assertThat(PathUtils.simplifyPathPattern("/archives/{year:\\d{4}}/page/{page:\\d+}"))
            .isEqualTo("/archives/{year}/page/{page}");
    }
}