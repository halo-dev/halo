package run.halo.app.core.attachment.thumbnail;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

@ExtendWith(MockitoExtension.class)
class DefaultThumbnailServiceTest {

    @Mock
    ReactiveExtensionClient client;

    @InjectMocks
    DefaultThumbnailService thumbnailService;

    @Test
    void shouldGetEmptyThumbnailIfNoAttachmentsFound() {
        Mockito.when(
                client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class))
            )
            .thenReturn(Flux.empty());
        thumbnailService.get(URI.create("/fake.png"))
            .as(StepVerifier::create)
            .expectNext(Map.of())
            .verifyComplete();

        thumbnailService.get(URI.create("/fake.png"))
            .as(StepVerifier::create)
            .expectNext(Map.of())
            .verifyComplete();

        // Only invoke once due to caching
        verify(client).listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class));
    }

    @Test
    void shouldGetThumbnailsIfAttachmentsFound() {
        Mockito.when(
                client.listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class))
            )
            .thenReturn(Flux.just(
                createAttachment("fake-png", "/fake.png", Map.of("s", "/fake.png?width=400")),
                createAttachment("fake-png", "/fake.png", Map.of("m", "/fake.png?width=800"))
            ));
        thumbnailService.get(URI.create("/fake.png"))
            .as(StepVerifier::create)
            .expectNext(Map.of(ThumbnailSize.S, URI.create("/fake.png?width=400")))
            .verifyComplete();

        thumbnailService.get(URI.create("/fake.png"))
            .as(StepVerifier::create)
            .expectNext(Map.of(ThumbnailSize.S, URI.create("/fake.png?width=400")))
            .verifyComplete();

        // Only invoke once due to caching
        verify(client).listAll(same(Attachment.class), isA(ListOptions.class), isA(Sort.class));
    }

    Attachment createAttachment(String name, String permalink, Map<String, String> thumbnails) {
        var attachment = new Attachment();
        attachment.setMetadata(new Metadata());
        attachment.getMetadata().setName(name);
        attachment.setSpec(new Attachment.AttachmentSpec());
        attachment.setStatus(new Attachment.AttachmentStatus());
        attachment.getStatus().setPermalink(permalink);
        attachment.getStatus().setThumbnails(thumbnails);
        return attachment;
    }

}