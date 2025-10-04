package run.halo.app.core.attachment.thumbnail;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.resource.ResourceTransformerChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.attachment.ThumbnailSize;

@ExtendWith(MockitoExtension.class)
class ThumbnailResourceTransformerTest {

    @Mock
    LocalThumbnailService localThumbnailService;

    @Mock
    ResourceTransformerChain transformerChain;

    @Mock
    Resource resource;

    @InjectMocks
    ThumbnailResourceTransformer thumbnailResourceTransformer;

    @Test
    void shouldNotTransformWithoutWidthQuery() {
        var exchange = MockServerWebExchange.builder(MockServerHttpRequest.get("/halo.png").build())
            .build();
        when(this.transformerChain.transform(exchange, this.resource))
            .thenReturn(Mono.just(this.resource));

        thumbnailResourceTransformer.transform(exchange, this.resource, this.transformerChain)
            .as(StepVerifier::create)
            .expectNext(this.resource)
            .verifyComplete();
    }

    @Test
    void shouldNotTransformWithNonFileResource() {
        var exchange = MockServerWebExchange.builder(
                MockServerHttpRequest.get("/halo.png").queryParam("width", "400").build())
            .build();
        when(this.resource.isFile()).thenReturn(false);
        when(this.transformerChain.transform(exchange, this.resource))
            .thenReturn(Mono.just(this.resource));

        thumbnailResourceTransformer.transform(exchange, this.resource, this.transformerChain)
            .as(StepVerifier::create)
            .expectNext(this.resource)
            .verifyComplete();
    }

    @Test
    void shouldNotTransformWithUnsupportedImageType() {
        var exchange = MockServerWebExchange.builder(
                MockServerHttpRequest.get("/halo.svg").queryParam("width", "400").build())
            .build();
        when(this.resource.isFile()).thenReturn(true);
        when(this.resource.getFilename()).thenReturn("halo.svg");
        when(this.transformerChain.transform(exchange, this.resource))
            .thenReturn(Mono.just(this.resource));

        thumbnailResourceTransformer.transform(exchange, this.resource, this.transformerChain)
            .as(StepVerifier::create)
            .expectNext(this.resource)
            .verifyComplete();
    }

    @Test
    void shouldReturnIfThumbnailExists() throws IOException {
        var exchange = MockServerWebExchange.builder(
                MockServerHttpRequest.get("/halo.png").queryParam("width", "400").build())
            .build();

        var attachmentRoot = Path.of("attachments").toAbsolutePath();
        var sourcePath = attachmentRoot.resolve("upload").resolve("halo.png");
        var thumbnailPath = attachmentRoot.resolve("thumbnails")
            .resolve("w400")
            .resolve("upload")
            .resolve("halo.png");
        when(this.localThumbnailService.resolveThumbnailPath(sourcePath, ThumbnailSize.S))
            .thenReturn(Optional.of(thumbnailPath));
        when(this.resource.isFile()).thenReturn(true);
        when(this.resource.getFilename()).thenReturn(sourcePath.getFileName().toString());
        when(this.resource.getURI()).thenReturn(sourcePath.toUri());
        thumbnailResourceTransformer = spy(thumbnailResourceTransformer);

        var thumbnailResource = mock(Resource.class);
        when(thumbnailResource.isReadable()).thenReturn(true);
        when(thumbnailResourceTransformer.createThumbnailResource(thumbnailPath))
            .thenReturn(thumbnailResource);

        when(this.transformerChain.transform(exchange, thumbnailResource))
            .thenReturn(Mono.just(thumbnailResource));

        thumbnailResourceTransformer.transform(exchange, this.resource, this.transformerChain)
            .as(StepVerifier::create)
            .expectNext(thumbnailResource)
            .verifyComplete();

        verify(this.localThumbnailService, never())
            .generate(any(Path.class), any(ThumbnailSize.class));
    }

    @Test
    void shouldGenerateIfThumbnailNotExists() throws IOException {
        var exchange = MockServerWebExchange.builder(
                MockServerHttpRequest.get("/halo.png").queryParam("width", "400").build())
            .build();

        var attachmentRoot = Path.of("attachments").toAbsolutePath();
        var sourcePath = attachmentRoot.resolve("upload").resolve("halo.png");
        var thumbnailPath = attachmentRoot.resolve("thumbnails")
            .resolve("w400")
            .resolve("upload")
            .resolve("halo.png");
        when(this.localThumbnailService.resolveThumbnailPath(sourcePath, ThumbnailSize.S))
            .thenReturn(Optional.of(thumbnailPath));
        when(this.resource.isFile()).thenReturn(true);
        when(this.resource.getFilename()).thenReturn(sourcePath.getFileName().toString());
        when(this.resource.getURI()).thenReturn(sourcePath.toUri());
        thumbnailResourceTransformer = spy(thumbnailResourceTransformer);

        var thumbnailResource = mock(Resource.class);
        when(thumbnailResource.isReadable()).thenReturn(false);
        when(thumbnailResourceTransformer.createThumbnailResource(thumbnailPath))
            .thenReturn(thumbnailResource);

        var generatedResource = mock(Resource.class);
        when(this.localThumbnailService.generate(sourcePath, ThumbnailSize.S))
            .thenReturn(Mono.just(generatedResource));
        when(this.transformerChain.transform(exchange, generatedResource))
            .thenReturn(Mono.just(generatedResource));

        thumbnailResourceTransformer.transform(exchange, this.resource, this.transformerChain)
            .as(StepVerifier::create)
            .expectNext(generatedResource)
            .verifyComplete();
    }
}