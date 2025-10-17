package run.halo.app.core.attachment.thumbnail;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
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
    void shouldReturnSourceIfEmptyGeneration() throws IOException {
        var exchange = MockServerWebExchange.builder(
                MockServerHttpRequest.get("/halo.png").queryParam("width", "400").build())
            .build();
        var attachmentRoot = Path.of("attachments").toAbsolutePath();
        var sourcePath = attachmentRoot.resolve("upload").resolve("halo.png");
        when(this.resource.isFile()).thenReturn(true);
        when(this.resource.getFilename()).thenReturn(sourcePath.getFileName().toString());
        when(this.resource.getURI()).thenReturn(sourcePath.toUri());
        thumbnailResourceTransformer = spy(thumbnailResourceTransformer);

        when(localThumbnailService.generate(sourcePath, ThumbnailSize.S)).thenReturn(Mono.empty());

        when(this.transformerChain.transform(eq(exchange), isA(Resource.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(1)));

        thumbnailResourceTransformer.transform(exchange, this.resource, this.transformerChain)
            .as(StepVerifier::create)
            .assertNext(resource -> assertDoesNotThrow(
                () -> assertEquals(sourcePath.toUri(), resource.getURI())
            ))
            .verifyComplete();
    }

    @Test
    void shouldReturnIfThumbnailExists() throws IOException {
        var exchange = MockServerWebExchange.builder(
                MockServerHttpRequest.get("/halo.png").queryParam("width", "400").build())
            .build();

        var attachmentRoot = Path.of("attachments").toAbsolutePath();
        var sourcePath = attachmentRoot.resolve("upload").resolve("halo.png");
        when(this.resource.isFile()).thenReturn(true);
        when(this.resource.getFilename()).thenReturn(sourcePath.getFileName().toString());
        when(this.resource.getURI()).thenReturn(sourcePath.toUri());
        thumbnailResourceTransformer = spy(thumbnailResourceTransformer);

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