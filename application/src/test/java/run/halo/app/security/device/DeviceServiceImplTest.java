package run.halo.app.security.device;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DeviceServiceImpl}.
 *
 * @author guqing
 * @since 2.17.0
 */
class DeviceServiceImplTest {

    @Test
    void deviceInfoParseTest() {
        var info = DeviceServiceImpl.DeviceInfo.parse(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like "
                + "Gecko) Chrome/126.0.0.0 Safari/537.36");
        assertThat(info.os()).isEqualTo("Mac OS X 10.15.7");
        assertThat(info.browser()).isEqualTo("Chrome 126.0");
    }

    @Test
    void deviceInfoParseOpenHarmonyTest() {
        var info = DeviceServiceImpl.DeviceInfo.parse(
            "Mozilla/5.0 (Phone; OpenHarmony 5.0) AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/114.0.0.0 Safari/537.36 ArkWeb/4.1.6.1 Mobile HuaweiBrowser/5.0.4.300");
        assertThat(info.os()).isEqualTo("OpenHarmony 5.0");
        assertThat(info.browser()).isEqualTo("Chrome 114.0");
    }
    
}