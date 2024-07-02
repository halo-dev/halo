package run.halo.app.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.web.reactive.server.WebTestClient;
import run.halo.app.search.event.HaloDocumentRebuildRequestEvent;

@ExtendWith(MockitoExtension.class)
class IndicesEndpointTest {

    @Mock
    ApplicationEventPublisher publisher;

    @InjectMocks
    IndicesEndpoint endpoint;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToRouterFunction(endpoint.endpoint()).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/indices/-/rebuild", "/indices/post"})
    void shouldRebuildIndices(String uri) {
        client.post().uri(uri)
            .exchange()
            .expectStatus().isAccepted();
        verify(publisher).publishEvent(assertArg(event -> {
            assertInstanceOf(HaloDocumentRebuildRequestEvent.class, event);
            assertEquals(endpoint, event.getSource());
        }));
    }

    @Test
    void ensureGroupVersionNotChanged() {
        assertEquals("api.console.halo.run/v1alpha1", endpoint.groupVersion().toString());
    }
}