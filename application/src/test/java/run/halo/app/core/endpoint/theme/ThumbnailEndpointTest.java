package run.halo.app.core.endpoint.theme;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
        webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint()).build();
    }

    @Test
    void shouldRedirectToRelativeUriWhenNoThumbnail() {
        when(thumbnailService.getSafeRedirectUri(URI.create("/myavatar.png"), ThumbnailSize.L))
                .thenReturn(Mono.just(URI.create("/myavatar.png")));
        webClient
                .get()
                .uri("/thumbnails/-/via-uri?size=l&uri=/myavatar.png")
                .exchange()
                .expectHeader()
                .location("/myavatar.png")
                .expectStatus()
                .is3xxRedirection();
    }

    @Test
    void shouldRedirectToStoredThumbnailForKnownExternalUri() {
        when(thumbnailService.getSafeRedirectUri(URI.create("https://cdn.example.com/a.png"), ThumbnailSize.M))
                .thenReturn(Mono.just(URI.create("https://cdn.example.com/a.png?w=800")));
        webClient
                .get()
                .uri(builder -> builder.path("/thumbnails/-/via-uri")
                        .queryParam("size", "m")
                        .queryParam("uri", "https://cdn.example.com/a.png")
                        .build())
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("https://cdn.example.com/a.png?w=800");
    }

    @Test
    void shouldReturnNotFoundForUnknownExternalUri() {
        when(thumbnailService.getSafeRedirectUri(URI.create("https://evil.example.com/phish"), ThumbnailSize.M))
                .thenReturn(Mono.empty());
        webClient
                .get()
                .uri(builder -> builder.path("/thumbnails/-/via-uri")
                        .queryParam("size", "m")
                        .queryParam("uri", "https://evil.example.com/phish")
                        .build())
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectHeader()
                .doesNotExist("Location");
    }

    @Test
    void shouldReturnNotFoundForSchemeRelativeExternalUri() {
        when(thumbnailService.getSafeRedirectUri(URI.create("//evil.example.com/a.png"), ThumbnailSize.M))
                .thenReturn(Mono.empty());
        webClient
                .get()
                .uri(builder -> builder.path("/thumbnails/-/via-uri")
                        .queryParam("size", "m")
                        .queryParam("uri", "//evil.example.com/a.png")
                        .build())
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectHeader()
                .doesNotExist("Location");
    }

    @Test
    void shouldPreservePercentEncodedPartsOfUri() {
        var permalink = "https://cdn.example.com/a%2Fb.png";
        when(thumbnailService.getSafeRedirectUri(URI.create(permalink), ThumbnailSize.M))
                .thenReturn(Mono.just(URI.create(permalink)));

        webClient
                .get()
                .uri(builder -> builder.path("/thumbnails/-/via-uri")
                        .queryParam("size", "m")
                        .queryParam("uri", permalink)
                        .build())
                .exchange()
                .expectStatus()
                .isFound();

        verify(thumbnailService).getSafeRedirectUri(URI.create(permalink), ThumbnailSize.M);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "https://cdn.example.com/%E4%B8%AD%E6%96%87/%E6%96%87%E4%BB%B6.png",
                "https://cdn.example.com/emoji-%F0%9F%98%80.png",
                "https://cdn.example.com/a%23b%3Fc%26d%3De%2Bf.png",
                "https://cdn.example.com/100%25.png",
                "https://cdn.example.com/space%20name.png"
            })
    void shouldPreserveComplexPercentEncodedUri(String permalink) {
        when(thumbnailService.getSafeRedirectUri(URI.create(permalink), ThumbnailSize.M))
                .thenReturn(Mono.just(URI.create(permalink)));

        webClient
                .get()
                .uri(builder -> builder.path("/thumbnails/-/via-uri")
                        .queryParam("size", "m")
                        .queryParam("uri", permalink)
                        .build())
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location(permalink);

        verify(thumbnailService).getSafeRedirectUri(URI.create(permalink), ThumbnailSize.M);
    }

    @Test
    void shouldReturnBadRequestForMalformedUri() {
        webClient
                .get()
                .uri(builder -> builder.path("/thumbnails/-/via-uri")
                        .queryParam("size", "m")
                        .queryParam("uri", "https://www.halo.run\u0000.evil.com/a.png")
                        .build())
                .exchange()
                .expectStatus()
                .isBadRequest();
    }
}
