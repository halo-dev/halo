package run.halo.app.infra.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link VersionUtils}.
 *
 * @author guqing
 * @since 2.2.0
 */
class VersionUtilsTest {

    @Test
    void satisfiesRequires() {
        // match all requires
        String systemVersion = "0.0.0";
        String requires = ">=2.2.0";
        boolean result = VersionUtils.satisfiesRequires(systemVersion, requires);
        assertThat(result).isTrue();

        systemVersion = "2.0.0";
        requires = "*";
        result = VersionUtils.satisfiesRequires(systemVersion, requires);
        assertThat(result).isTrue();

        systemVersion = "2.0.0";
        requires = "";
        result = VersionUtils.satisfiesRequires(systemVersion, requires);
        assertThat(result).isTrue();

        // match exact version
        systemVersion = "2.0.0";
        requires = ">=2.0.0";
        result = VersionUtils.satisfiesRequires(systemVersion, requires);
        assertThat(result).isTrue();

        systemVersion = "2.0.0";
        requires = ">2.0.0";
        result = VersionUtils.satisfiesRequires(systemVersion, requires);
        assertThat(result).isFalse();

        //an exact version x.y.z will implicitly mean the same as >=x.y.z
        systemVersion = "2.1.0";
        // means >=2.0.0
        requires = "2.0.0";
        result = VersionUtils.satisfiesRequires(systemVersion, requires);
        assertThat(result).isTrue();
    }
}
