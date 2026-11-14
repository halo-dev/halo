package run.halo.app.infra.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ExternalUrlUtilsTest {

    @ParameterizedTest
    @MethodSource("urlsToConvert")
    void shouldConvertHostToAscii(String externalUrl, String expected) {
        assertThat(ExternalUrlUtils.toAscii(externalUrl)).isEqualTo(expected);
    }

    static Stream<Arguments> urlsToConvert() {
        return Stream.of(
                Arguments.of("https://abcd.中国", "https://abcd.xn--fiqs8s"),
                Arguments.of("https://白日梦.cn/a/b?q=1#f", "https://xn--wgv44dw7s.cn/a/b?q=1#f"),
                Arguments.of("https://abcd.中国:8443/path", "https://abcd.xn--fiqs8s:8443/path"),
                Arguments.of("https://user@abcd.中国:8443/path", "https://user@abcd.xn--fiqs8s:8443/path"),
                Arguments.of("https://a.中国.b.cn", "https://a.xn--fiqs8s.b.cn"),
                Arguments.of("https://xn--wgv44dw7s.cn", "https://xn--wgv44dw7s.cn"),
                Arguments.of("https://halo.run/path", "https://halo.run/path"),
                Arguments.of("https://halo.run/中文", "https://halo.run/中文"),
                Arguments.of("https://[::1]:8080/path", "https://[::1]:8080/path"),
                Arguments.of(
                        "https://halo.run/path?next=https://abcd.中国", "https://halo.run/path?next=https://abcd.中国"));
    }

    @Test
    void shouldKeepOriginalUrlWhenCannotBeConverted() {
        assertThat(ExternalUrlUtils.toAscii(null)).isNull();
        assertThat(ExternalUrlUtils.toAscii("")).isEmpty();
        assertThat(ExternalUrlUtils.toAscii("halo.run")).isEqualTo("halo.run");
        assertThat(ExternalUrlUtils.toAscii("https://")).isEqualTo("https://");
        assertThat(ExternalUrlUtils.toAscii("https:///path")).isEqualTo("https:///path");
        assertThat(ExternalUrlUtils.toAscii("https://:8080/path")).isEqualTo("https://:8080/path");
    }
}
