package run.halo.app.theme.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import run.halo.app.extension.GroupVersion;
import run.halo.app.theme.finders.PluginFinder;

/**
 * Tests for {@link PluginQueryEndpoint}.
 *
 * @author guqing
 * @since 2.5.0
 */
@ExtendWith(MockitoExtension.class)
class PluginQueryEndpointTest {

    @Mock
    private PluginFinder pluginFinder;

    @InjectMocks
    private PluginQueryEndpoint endpoint;

    private WebTestClient webClient;

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint()).build();
    }

    @Test
    void available() {
        when(pluginFinder.available("fake-plugin")).thenReturn(true);
        webClient.get().uri("/plugins/fake-plugin/available")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$").isEqualTo(true);
    }

    @Test
    void groupVersion() {
        GroupVersion groupVersion = endpoint.groupVersion();
        assertThat(groupVersion.toString()).isEqualTo("api.plugin.halo.run/v1alpha1");
    }
}