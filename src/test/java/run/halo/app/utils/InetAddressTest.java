package run.halo.app.utils;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * InetAddress test.
 *
 * @author johnniang
 */
public class InetAddressTest {

    @Test
    public void getMachaineAddressTest() throws UnknownHostException {
        InetAddress localHost = InetAddress.getLocalHost();
        System.out.println("Localhost: " + localHost.getHostAddress());

        InetAddress loopbackAddress = InetAddress.getLoopbackAddress();
        System.out.println("Loopback: " + loopbackAddress.getHostAddress());
    }
}
