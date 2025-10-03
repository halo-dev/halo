package run.halo.app.core.endpoint.theme;

import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.attachment.thumbnail.ThumbnailService;

/**
 * Tests for {@link ThumbnailEndpoint}.
 *
 * @author guqing
 * @since 2.19.0
 */
@ExtendWith(MockitoExtension.class)
class ThumbnailEndpointTest {

    WebTestClient webClient;

    @Mock
    private ThumbnailService thumbnailService;

    @InjectMocks
    private ThumbnailEndpoint endpoint;

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint())
            .build();
    }

    @Test
    void thumbnailUriNotAccessible() {
        when(thumbnailService.get(URI.create("/myavatar.png"), ThumbnailSize.L))
            .thenReturn(Mono.empty());
        webClient.get()
            .uri("/thumbnails/-/via-uri?size=l&uri=/myavatar.png")
            .exchange()
            .expectHeader().location("/myavatar.png")
            .expectStatus().is3xxRedirection();
    }
}
