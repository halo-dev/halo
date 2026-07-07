package run.halo.app.core.endpoint.console;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.AttachmentPermalinkMatchResult;
import run.halo.app.core.attachment.AttachmentPermalinkMatcher;
import run.halo.app.core.endpoint.AttachmentHandler;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.SystemConfigFetcher;

@ExtendWith(MockitoExtension.class)
class AttachmentConsoleEndpointTest {

    @Mock
    SystemConfigFetcher systemConfigFetcher;

    @Mock
    AttachmentService attachmentService;

    @Mock
    AttachmentPermalinkMatcher attachmentPermalinkMatcher;

    @Mock
    ExternalUrlSupplier externalUrlSupplier;

    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        var attachmentHandler =
                new AttachmentHandler(attachmentService, attachmentPermalinkMatcher, externalUrlSupplier);
        var endpoint = new AttachmentConsoleEndpoint(systemConfigFetcher, attachmentHandler);
        webTestClient = WebTestClient.bindToRouterFunction(endpoint.endpoint()).build();
    }

    @Test
    void shouldMatchPermalinks() throws Exception {
        var siteUrl = URI.create("https://www.halo.run").toURL();
        when(externalUrlSupplier.getURL(any())).thenReturn(siteUrl);
        when(attachmentPermalinkMatcher.match(List.of("https://cdn.example.com/halo.png"), siteUrl))
                .thenReturn(Mono.just(
                        List.of(new AttachmentPermalinkMatchResult("https://cdn.example.com/halo.png", true))));

        webTestClient
                .post()
                .uri("/attachments/-/match-permalinks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {"urls":["https://cdn.example.com/halo.png"]}
                    """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.items[0].url")
                .isEqualTo("https://cdn.example.com/halo.png")
                .jsonPath("$.items[0].matched")
                .isEqualTo(true)
                .jsonPath("$.items[0].metadata")
                .doesNotExist();
    }
}
