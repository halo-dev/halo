package run.halo.app.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author ryanwang
 * @date 2020-02-03
 */
public class VersionUtilTest {

    @Test
    public void compareVersion() {
        Assert.assertTrue(VersionUtil.compareVersion("1.2.0", "1.1.1"));

        Assert.assertTrue(VersionUtil.compareVersion("1.2.1", "1.2.0"));

        Assert.assertTrue(VersionUtil.compareVersion("1.2.0", "1.1.1.0"));

        Assert.assertTrue(VersionUtil.compareVersion("1.2.0", "0.4.4"));

        Assert.assertFalse(VersionUtil.compareVersion("1.1.1", "1.2.0"));

        Assert.assertFalse(VersionUtil.compareVersion("0.0.1", "1.2.0"));
    }
}