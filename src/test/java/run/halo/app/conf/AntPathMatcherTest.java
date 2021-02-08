package run.halo.app.conf;

import org.junit.jupiter.api.Test;
import org.springframework.util.AntPathMatcher;

import static org.junit.jupiter.api.Assertions.*;

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
