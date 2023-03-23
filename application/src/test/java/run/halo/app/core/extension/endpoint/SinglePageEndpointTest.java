package run.halo.app.core.extension.endpoint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Tests for @{@link SinglePageEndpoint}.
 *
 * @author guqing
 * @since 2.3.0
 */
@ExtendWith(MockitoExtension.class)
class SinglePageEndpointTest {

    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    SinglePageEndpoint singlePageEndpoint;

    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient
            .bindToRouterFunction(singlePageEndpoint.endpoint())
            .build();
    }

    @Test
    void publishRetryOnOptimisticLockingFailure() {
        var page = new SinglePage();
        page.setMetadata(new Metadata());
        page.getMetadata().setName("page-1");
        page.setSpec(new SinglePage.SinglePageSpec());
        when(client.get(eq(SinglePage.class), eq("page-1"))).thenReturn(Mono.just(page));

        when(client.update(any(SinglePage.class)))
            .thenReturn(Mono.error(new OptimisticLockingFailureException("fake-error")));

        // Send request
        webTestClient.put()
            .uri("/singlepages/{name}/publish?async=false", "page-1")
            .exchange()
            .expectStatus()
            .is5xxServerError();

        // Verify WebClient retry behavior
        verify(client, times(6)).get(eq(SinglePage.class), eq("page-1"));
        verify(client, times(6)).update(any(SinglePage.class));
    }

    @Test
    void publishSuccess() {
        var page = new SinglePage();
        page.setMetadata(new Metadata());
        page.getMetadata().setName("page-1");
        page.setSpec(new SinglePage.SinglePageSpec());

        when(client.get(eq(SinglePage.class), eq("page-1"))).thenReturn(Mono.just(page));
        when(client.fetch(eq(SinglePage.class), eq("page-1"))).thenReturn(Mono.empty());

        when(client.update(any(SinglePage.class))).thenReturn(Mono.just(page));

        // Send request
        webTestClient.put()
            .uri("/singlepages/{name}/publish?async=false", "page-1")
            .exchange()
            .expectStatus()
            .is2xxSuccessful();

        // Verify WebClient retry behavior
        verify(client, times(1)).get(eq(SinglePage.class), eq("page-1"));
        verify(client, times(1)).update(any(SinglePage.class));
    }
}