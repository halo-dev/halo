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
}