package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * InetAddress test.
 *
 * @author johnniang
 */
@Slf4j
class InetAddressTest {

    @Test
    void getMachineAddressTest() throws UnknownHostException {
        InetAddress localHost = InetAddress.getLocalHost();
        log.debug("Localhost: " + localHost.getHostAddress());

        InetAddress loopbackAddress = InetAddress.getLoopbackAddress();
        log.debug("Loopback: " + loopbackAddress.getHostAddress());
    }
}
