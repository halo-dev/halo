package run.halo.app.core.endpoint.uc;

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
import run.halo.app.content.PostService;
import run.halo.app.core.attachment.AttachmentLister;
import run.halo.app.core.attachment.AttachmentPermalinkMatchResult;
import run.halo.app.core.attachment.AttachmentPermalinkMatcher;
import run.halo.app.core.endpoint.AttachmentHandler;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.SystemConfigFetcher;

@ExtendWith(MockitoExtension.class)
class AttachmentUcEndpointTest {

    @Mock
    AttachmentService attachmentService;

    @Mock
    AttachmentLister attachmentLister;

    @Mock
    PostService postService;

    @Mock
    SystemConfigFetcher systemConfigFetcher;

    @Mock
    AttachmentPermalinkMatcher attachmentPermalinkMatcher;

    @Mock
    ExternalUrlSupplier externalUrlSupplier;

    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        var attachmentHandler =
                new AttachmentHandler(attachmentService, attachmentPermalinkMatcher, externalUrlSupplier);
        var endpoint = new AttachmentUcEndpoint(
                attachmentService, attachmentLister, postService, systemConfigFetcher, attachmentHandler);
        webTestClient = WebTestClient.bindToRouterFunction(endpoint.endpoint()).build();
    }

    @Test
    void shouldMatchPermalinks() throws Exception {
        var siteUrl = URI.create("https://www.halo.run").toURL();
        when(externalUrlSupplier.getURL(any())).thenReturn(siteUrl);
        when(attachmentPermalinkMatcher.match(List.of("/upload/halo.png"), siteUrl))
                .thenReturn(Mono.just(List.of(new AttachmentPermalinkMatchResult("/upload/halo.png", false))));

        webTestClient
                .post()
                .uri("/attachments/-/match-permalinks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                    {"urls":["/upload/halo.png"]}
                    """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.items[0].url")
                .isEqualTo("/upload/halo.png")
                .jsonPath("$.items[0].matched")
                .isEqualTo(false)
                .jsonPath("$.items[0].metadata")
                .doesNotExist();
    }
}
