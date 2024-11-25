package run.halo.app.core.attachment.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.extension.index.query.QueryFactory.equal;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.attachment.LocalThumbnailProvider;
import run.halo.app.core.attachment.LocalThumbnailService;
import run.halo.app.core.attachment.ThumbnailProvider;
import run.halo.app.core.attachment.ThumbnailSigner;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.attachment.extension.Thumbnail;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ExternalLinkProcessor;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

/**
 * Tests for {@link ThumbnailServiceImpl}.
 *
 * @author guqing
 * @since 2.19.0
 */
@ExtendWith(MockitoExtension.class)
class ThumbnailServiceImplTest {
    @Mock
    private ExternalLinkProcessor externalLinkProcessor;

    @Mock
    private ExtensionGetter extensionGetter;

    @Mock
    private LocalThumbnailProvider localThumbnailProvider;

    @Mock
    private LocalThumbnailService localThumbnailService;

    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    private ThumbnailServiceImpl thumbnailService;

    @Test
    void toImageUrl() {
        var link = "/test.jpg";
        when(externalLinkProcessor.processLink(link)).thenReturn("http://localhost:8090/test.jpg");
        var imageUrl = thumbnailService.toImageUrl(URI.create(link));
        assertThat(imageUrl).isPresent();
        assertThat(imageUrl.get().toString()).isEqualTo("http://localhost:8090/test.jpg");

        var absoluteLink = "https://halo.run/test.jpg";
        imageUrl = thumbnailService.toImageUrl(URI.create(absoluteLink));
        assertThat(imageUrl).isPresent();
        assertThat(imageUrl.get().toString()).isEqualTo(absoluteLink);
    }

    @Test
    void generateTest() {
        var uri = URI.create("http://localhost:8090/test.jpg");
        var size = ThumbnailSize.L;
        when(localThumbnailService.ensureInSiteUriIsRelative(eq(uri)))
            .thenReturn(uri);
        var imageHash = ThumbnailSigner.generateSignature(uri.toString());
        var id = Thumbnail.idIndexFunc(imageHash, size.name());
        var listOptions = ListOptions.builder()
            .fieldQuery(equal(Thumbnail.ID_INDEX, id))
            .build();
        when(client.listBy(eq(Thumbnail.class), any(), any())).thenReturn(Mono.empty());

        var spyThumbnailService = spy(thumbnailService);
        doReturn(Mono.empty()).when(spyThumbnailService).create(any(), any());

        spyThumbnailService.generate(uri, size)
            .as(StepVerifier::create)
            .verifyComplete();

        verify(client).listBy(eq(Thumbnail.class), assertArg(options -> {
            assertThat(options.toString()).isEqualTo(listOptions.toString());
        }), isA(PageRequest.class));
    }

    @Test
    void createTest() throws MalformedURLException, URISyntaxException {
        var url = new URL("http://localhost:8090/test.jpg");
        when(extensionGetter.getEnabledExtensions(eq(ThumbnailProvider.class)))
            .thenReturn(Flux.just(localThumbnailProvider));
        var thumbUri = URI.create("/test-thumb.jpg");
        when(localThumbnailProvider.generate(any())).thenReturn(Mono.just(thumbUri));
        when(localThumbnailProvider.supports(any())).thenReturn(Mono.just(true));

        var insiteUri = URI.create("/test.jpg");
        when(localThumbnailService.ensureInSiteUriIsRelative(any()))
            .thenReturn(insiteUri);
        when(client.create(any())).thenReturn(Mono.empty());

        when(client.listBy(eq(Thumbnail.class), any(), isA(PageRequest.class)))
            .thenReturn(Mono.empty());

        thumbnailService.create(url, ThumbnailSize.M)
            .as(StepVerifier::create)
            .expectNext(thumbUri)
            .verifyComplete();

        thumbnailService.fetchThumbnail(url.toURI(), ThumbnailSize.M)
            .as(StepVerifier::create)
            .verifyComplete();
        var hash = ThumbnailSigner.generateSignature(insiteUri.toString());

        verify(client, times(2)).listBy(eq(Thumbnail.class),
            assertArg(options -> {
                var exceptOptions = ListOptions.builder()
                    .fieldQuery(equal(Thumbnail.ID_INDEX,
                        Thumbnail.idIndexFunc(hash, ThumbnailSize.M.name())
                    ))
                    .build();
                assertThat(options.toString()).isEqualTo(exceptOptions.toString());
            }), isA(PageRequest.class));

        verify(localThumbnailProvider).generate(any());

        verify(client).create(assertArg(thumb -> {
            JSONAssert.assertEquals("""
                {
                    "spec": {
                        "imageSignature": "%s",
                        "imageUri": "/test.jpg",
                        "size": "M",
                        "thumbnailUri": "/test-thumb.jpg"
                    },
                    "apiVersion": "storage.halo.run/v1alpha1",
                    "kind": "Thumbnail",
                    "metadata": {
                        "generateName": "thumb-"
                    }
                }
                """.formatted(hash), JsonUtils.objectToJson(thumb), true);
        }));
    }

    @Test
    void createTest2() throws MalformedURLException {
        when(extensionGetter.getEnabledExtensions(eq(ThumbnailProvider.class)))
            .thenReturn(Flux.empty());

        // no thumbnail provider will do nothing
        var url = new URL("http://localhost:8090/test.jpg");
        thumbnailService.create(url, ThumbnailSize.M)
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Nested
    class ThumbnailGenerateConcurrencyTest {

        @Test
        public void concurrentThumbnailGeneration() throws InterruptedException {
            var spyThumbnailService = spy(thumbnailService);

            URI imageUri = URI.create("http://localhost:8090/test.jpg");

            doReturn(Mono.empty()).when(spyThumbnailService).fetchThumbnail(eq(imageUri), any());

            var createdUri = URI.create("/test-thumb.jpg");
            doReturn(Mono.just(createdUri)).when(spyThumbnailService).create(any(), any());

            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            var latch = new CountDownLatch(threadCount);

            var results = new ConcurrentLinkedQueue<Mono<URI>>();

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        results.add(spyThumbnailService.generate(imageUri, ThumbnailSize.M));
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();

            results.forEach(result -> {
                StepVerifier.create(result)
                    .expectNext(createdUri)
                    .verifyComplete();
            });

            verify(spyThumbnailService).fetchThumbnail(eq(imageUri), eq(ThumbnailSize.M));
            verify(spyThumbnailService).create(any(), eq(ThumbnailSize.M));

            executor.shutdown();
        }
    }
}