package run.halo.app.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import run.halo.app.model.support.HaloConst;

/**
 * @author ryanwang
 * @date 2020-02-03
 */
class VersionUtilTest {

    @Test
    void hasSameMajorAndMinorVersionTest() {
        assertThat(
            VersionUtil.hasSameMajorAndMinorVersion(HaloConst.UNKNOWN_VERSION, "1.4.0")).isFalse();

        assertThat(VersionUtil.hasSameMajorAndMinorVersion("1.3.1", "1.5.8")).isFalse();

        assertThat(VersionUtil.hasSameMajorAndMinorVersion("1.2.4.alpha-1", "1.3.5")).isFalse();

        assertThat(VersionUtil.hasSameMajorAndMinorVersion("0.0.4", "0.0.1")).isTrue();

        assertThat(VersionUtil.hasSameMajorAndMinorVersion("1.0.4", "1.0.5-alpha.1")).isTrue();

        assertThat(VersionUtil.hasSameMajorAndMinorVersion("1.0.4", "1.0.5-rc.1")).isTrue();

        assertThat(VersionUtil.hasSameMajorAndMinorVersion("2.0.0", "2.0.1")).isTrue();

        assertThat(VersionUtil.hasSameMajorAndMinorVersion(null, null)).isTrue();
    }

}