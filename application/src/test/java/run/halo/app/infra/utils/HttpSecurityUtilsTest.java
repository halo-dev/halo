package run.halo.app.infra.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.InetAddress;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HttpSecurityUtilsTest {

    @ParameterizedTest
    @CsvSource({
        "127.0.0.1, true",
        "0.0.0.1, true",
        "100.64.0.1, true",
        "169.254.10.20, true",
        "192.168.1.10, true",
        "198.18.0.1, true",
        "224.0.0.1, true",
        "240.0.0.1, true",
        "255.255.255.255, true",
        "::1, true",
        "fc00::1, true",
        "93.184.216.34, false",
        "8.8.8.8, false",
        "2001:4860:4860::8888, false"
    })
    void isBlockedTest(String host, boolean isBlocked) throws Exception {
        assertEquals(isBlocked, HttpSecurityUtils.isBlocked(InetAddress.getByName(host)));
    }

}
