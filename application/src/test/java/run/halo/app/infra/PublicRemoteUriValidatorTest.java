package run.halo.app.infra;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import org.junit.jupiter.api.Test;

class PublicRemoteUriValidatorTest {

    private final PublicRemoteUriValidator validator = new PublicRemoteUriValidator();

    @Test
    void shouldAllowPublicHttpsAddress() {
        assertDoesNotThrow(() -> validator.validate(URI.create("https://93.184.216.34/file.zip")));
    }

    @Test
    void shouldRejectLoopbackHost() {
        assertThrows(IllegalArgumentException.class,
            () -> validator.validate(URI.create("http://127.0.0.1/file.zip")));
    }

    @Test
    void shouldRejectLocalhostHost() {
        assertThrows(IllegalArgumentException.class,
            () -> validator.validate(URI.create("http://localhost/file.zip")));
    }

    @Test
    void shouldRejectPrivateIpv4Address() {
        assertThrows(IllegalArgumentException.class,
            () -> validator.validate(URI.create("http://192.168.1.10/file.zip")));
    }

    @Test
    void shouldRejectUniqueLocalIpv6Address() {
        assertThrows(IllegalArgumentException.class,
            () -> validator.validate(URI.create("http://[fd00::1]/file.zip")));
    }

    @Test
    void shouldRejectNonHttpScheme() {
        assertThrows(IllegalArgumentException.class,
            () -> validator.validate(URI.create("file:///tmp/file.zip")));
    }
}
