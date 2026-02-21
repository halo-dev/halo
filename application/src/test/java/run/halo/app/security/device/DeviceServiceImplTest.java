package run.halo.app.security.device;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;

/**
 * Tests for {@link DeviceServiceImpl}.
 *
 * @author guqing
 * @since 2.17.0
 */
class DeviceServiceImplTest {

    static Stream<Arguments> deviceInfoParseTest() {
        return Stream.of(
            Arguments.of(
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like "
                    + "Gecko) Chrome/126.0.0.0 Safari/537.36",
                "Mac OS X 10.15.7",
                "Chrome 126.0"
            ),
            Arguments.of(
                "Mozilla/5.0 (Phone; OpenHarmony 5.0) AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/114.0.0.0 Safari/537.36 ArkWeb/4.1.6.1 Mobile HuaweiBrowser/5.0.4"
                    + ".300",
                "OpenHarmony 5.0",
                "Chrome 114.0"
            )
        );
    }

    @ParameterizedTest
    @MethodSource
    void deviceInfoParseTest(String userAgent, String expectedOs, String expectedBrowser) {
        var info = DeviceServiceImpl.DeviceInfo.parse(userAgent);
        assertThat(info.os()).isEqualTo(expectedOs);
        assertThat(info.browser()).isEqualTo(expectedBrowser);
    }

    @Test
    void shouldDetectWindows10WithoutClientHints() {
        var ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";
        var info = DeviceServiceImpl.DeviceInfo.parse(ua);
        assertThat(info.os()).isEqualTo("Windows 10");
        assertThat(info.browser()).isEqualTo("Chrome 126.0");
    }

    @Test
    void shouldDetectWindows11WithClientHints() {
        var ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";
        var headers = new HttpHeaders();
        headers.set("Sec-CH-UA-Platform-Version", "\"15.0.0\"");
        var info = DeviceServiceImpl.DeviceInfo.parse(ua, headers);
        assertThat(info.os()).isEqualTo("Windows 11");
    }

    @Test
    void shouldDetectWindows10WithClientHintsLowVersion() {
        var ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";
        var headers = new HttpHeaders();
        headers.set("Sec-CH-UA-Platform-Version", "\"10.0.0\"");
        var info = DeviceServiceImpl.DeviceInfo.parse(ua, headers);
        assertThat(info.os()).isEqualTo("Windows 10");
    }

    @Test
    void shouldDetectOlderWindowsVersions() {
        var ua7 = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/100.0.0.0 Safari/537.36";
        assertThat(DeviceServiceImpl.DeviceInfo.parse(ua7).os()).isEqualTo("Windows 7");

        var ua81 = "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/100.0.0.0 Safari/537.36";
        assertThat(DeviceServiceImpl.DeviceInfo.parse(ua81).os()).isEqualTo("Windows 8.1");
    }
}