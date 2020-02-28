package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * InetAddress test.
 *
 * @author johnniang
 */
@Slf4j
public class InetAddressTest {

    @Test
    public void getMachaineAddressTest() throws UnknownHostException {
        InetAddress localHost = InetAddress.getLocalHost();
        log.debug("Localhost: " + localHost.getHostAddress());

        InetAddress loopbackAddress = InetAddress.getLoopbackAddress();
        log.debug("Loopback: " + loopbackAddress.getHostAddress());
    }
}
