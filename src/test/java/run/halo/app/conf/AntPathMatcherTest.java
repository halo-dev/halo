package run.halo.app.conf;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.util.AntPathMatcher;

/**
 * Ant path matcher test.
 *
 * @author johnniang
 */
class AntPathMatcherTest {

    final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Test
    void matchTest() {
        assertAll(
            () -> assertFalse(pathMatcher.match("/admin/?*/**", "/admin")),
            () -> assertFalse(pathMatcher.match("/admin/?*/**", "/admin/")),

            () -> assertTrue(pathMatcher.match("/admin/?*/**", "/admin/index.html")),
            () -> assertTrue(pathMatcher.match("/admin/?*/**", "/admin/index.html/more"))
        );
    }
}
