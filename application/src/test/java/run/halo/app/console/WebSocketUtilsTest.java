package run.halo.app.console;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class WebSocketUtilsTest {

    @Nested
    class IsWebSocketTest {

        @Test
        void shouldBeWebSocketIfHeadersContaining() {
            var headers = new HttpHeaders();
            headers.add("Connection", "Upgrade");
            headers.add("Upgrade", "websocket");
            assertTrue(WebSocketUtils.isWebSocketUpgrade(headers));
        }

        @Test
        void shouldNotBeWebSocketIfHeaderValuesAreIncorrect() {
            var headers = new HttpHeaders();
            headers.add("Connection", "keep-alive");
            headers.add("Upgrade", "websocket");
            assertFalse(WebSocketUtils.isWebSocketUpgrade(headers));
        }

        @Test
        void shouldNotBeWebSocketIfMissingUpgradeHeader() {
            var headers = new HttpHeaders();
            headers.add("Connection", "Upgrade");
            assertFalse(WebSocketUtils.isWebSocketUpgrade(headers));
        }

        @Test
        void shouldNotBeWebSocketIfMissingConnectionHeader() {
            var headers = new HttpHeaders();
            headers.add("Connection", "Upgrade");
            assertFalse(WebSocketUtils.isWebSocketUpgrade(headers));
        }

        @Test
        void shouldNotBeWebSocketIfMissingHeaders() {
            var headers = new HttpHeaders();
            assertFalse(WebSocketUtils.isWebSocketUpgrade(headers));
        }
    }
}