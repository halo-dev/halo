package run.halo.app.core.endpoint;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import run.halo.app.extension.GroupVersion;

@ExtendWith(MockitoExtension.class)
class WebSocketHandlerMappingTest {

    @InjectMocks
    WebSocketHandlerMapping handlerMapping;

    @Test
    void shouldRegisterEndpoint() {
        var endpoint = new FakeWebSocketEndpoint();
        handlerMapping.register(List.of(endpoint));
        assertTrue(handlerMapping.getEndpointMap().containsValue(endpoint));
    }

    @Test
    void shouldUnregisterEndpoint() {
        var endpoint = new FakeWebSocketEndpoint();
        handlerMapping.register(List.of(endpoint));
        assertTrue(handlerMapping.getEndpointMap().containsValue(endpoint));
        handlerMapping.unregister(List.of(endpoint));
        assertFalse(handlerMapping.getEndpointMap().containsValue(endpoint));
    }

    static class FakeWebSocketEndpoint implements WebSocketEndpoint {

        @Override
        public String urlPath() {
            return "/resources";
        }

        @Override
        public GroupVersion groupVersion() {
            return GroupVersion.parseAPIVersion("fake.halo.run/v1alpha1");
        }

        @Override
        public WebSocketHandler handler() {
            return WebSocketSession::close;
        }
    }

}