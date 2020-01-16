package run.halo.app.conf;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.AntPathMatcher;

/**
 * Ant path matcher test.
 *
 * @author johnniang
 */
public class AntPathMatcherTest {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Test
    public void matchTest() {
        Assert.assertFalse(pathMatcher.match("/admin/?*/**", "/admin"));
        Assert.assertFalse(pathMatcher.match("/admin/?*/**", "/admin/"));

        Assert.assertTrue(pathMatcher.match("/admin/?*/**", "/admin/index.html"));
        Assert.assertTrue(pathMatcher.match("/admin/?*/**", "/admin/index.html/more"));
    }
}
