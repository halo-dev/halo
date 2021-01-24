package run.halo.app.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import run.halo.app.model.support.HaloConst;

/**
 * @author ryanwang
 * @date 2020-02-03
 */
class VersionUtilTest {

    @Test
    void compareVersion() {
        assertTrue(VersionUtil.compareVersion("1.2.0", "1.1.1"));
        assertTrue(VersionUtil.compareVersion("1.2.1", "1.2.0"));
        assertTrue(VersionUtil.compareVersion("1.2.0", "1.1.1"));
        assertTrue(VersionUtil.compareVersion("1.2.0", "0.4.4"));
        assertFalse(VersionUtil.compareVersion("1.1.1", "1.2.0"));
        assertFalse(VersionUtil.compareVersion("0.0.1", "1.2.0"));
    }

    @Test
    void unknownVersionCompareTest() {
        // build a random version
        String randomVersion = String.join(".",
            RandomStringUtils.randomNumeric(1),
            RandomStringUtils.randomNumeric(2),
            RandomStringUtils.randomNumeric(3));
        assertTrue(VersionUtil.compareVersion(HaloConst.UNKNOWN_VERSION, randomVersion));
    }

}