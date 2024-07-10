package run.halo.app.security.device;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

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
    void test() {
        var instant = Instant.parse("2024-07-10T00:30:00.00Z");
        assertThat(instant.toString()).isEqualTo("2024-07-10T00:30:00Z");
        var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss")
            .withZone(ZoneOffset.systemDefault());
        var format = dateTimeFormatter.format(instant);
        assertThat(format).isEqualTo("2024/07/10 08:30:00");
    }
}