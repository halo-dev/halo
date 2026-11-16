package run.halo.app.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.search.event.HaloDocumentDeleteRequestEvent;
import run.halo.app.search.lucene.LuceneSearchEngine;

@ExtendWith(OutputCaptureExtension.class)
class SearchVariantRecoveryTest {

    @TempDir
    Path directory;

    private ExtensionGetter extensions;
    private LuceneSearchEngine engine;
    private HaloDocumentEventsListener listener;

    @BeforeEach
    void setUp() throws Exception {
        engine = new LuceneSearchEngine(directory);
        engine.afterPropertiesSet();
        extensions = mock(ExtensionGetter.class);
        when(extensions.getEnabledExtension(SearchEngine.class)).thenReturn(Mono.just(engine));
        listener = new HaloDocumentEventsListener(extensions);
    }

    @AfterEach
    void close() throws Exception {
        listener.shutdown();
        engine.destroy();
    }

    @Test
    void retriesProviderLookupAndIndexesRecoveredTranslation() {
        var attempts = new AtomicInteger();
        HaloDocumentVariantProvider provider = source -> Flux.just(variant());
        when(extensions.getEnabledExtension(HaloDocumentVariantProvider.class)).thenAnswer(ignored -> {
            if (attempts.incrementAndGet() < 3) {
                return Mono.error(new IllegalStateException("Temporary extension lookup failure"));
            }
            return Mono.just(provider);
        });

        index();

        assertThat(attempts).hasValue(3);
        assertThat(total("sourcekeyword", null)).isEqualTo(1);
        assertThat(total("variantkeyword", "en")).isEqualTo(1);
    }

    @Test
    void retriesTemporaryReadFailureWithoutRequiringAnotherSourceEvent() {
        var attempts = new AtomicInteger();
        HaloDocumentVariantProvider provider = source -> {
            if (attempts.incrementAndGet() == 1) {
                return Flux.error(new IllegalStateException("Temporary translation store failure"));
            }
            return Flux.just(variant());
        };
        when(extensions.getEnabledExtension(HaloDocumentVariantProvider.class)).thenReturn(Mono.just(provider));

        index();

        assertThat(attempts).hasValue(2);
        assertThat(total("sourcekeyword", null)).isEqualTo(1);
        assertThat(total("variantkeyword", "en")).isEqualTo(1);
    }

    @Test
    void retriesTimedOutReadsWithinTheBoundedAttemptCount() {
        var attempts = new AtomicInteger();
        HaloDocumentVariantProvider provider = source -> {
            if (attempts.incrementAndGet() < 3) {
                return Flux.never();
            }
            return Flux.just(variant());
        };
        when(extensions.getEnabledExtension(HaloDocumentVariantProvider.class)).thenReturn(Mono.just(provider));

        StepVerifier.withVirtualTime(() -> listener.indexDocuments(Flux.just(document()), engine))
                .thenAwait(Duration.ofMillis(10_750))
                .expectComplete()
                .verify(Duration.ofSeconds(5));

        assertThat(attempts).hasValue(3);
        assertThat(total("sourcekeyword", null)).isEqualTo(1);
        assertThat(total("variantkeyword", "en")).isEqualTo(1);
    }

    @Test
    void exhaustedReadsLogFailureKeepSourceAndAllowQueuedDeletion(CapturedOutput output) throws Exception {
        var attempts = new AtomicInteger();
        HaloDocumentVariantProvider provider = source -> {
            attempts.incrementAndGet();
            return Flux.error(new IllegalStateException("Persistent translation store failure"));
        };
        when(extensions.getEnabledExtension(HaloDocumentVariantProvider.class)).thenReturn(Mono.just(provider));

        index();

        assertThat(attempts).hasValue(3);
        assertThat(total("sourcekeyword", null)).isEqualTo(1);
        assertThat(total("variantkeyword", "en")).isZero();
        assertThat(output.getAll())
                .contains("Search variants unavailable for document source; indexing source only")
                .contains("Persistent translation store failure");

        var deleted = new CountDownLatch(1);
        listener.onApplicationEvent(new HaloDocumentDeleteRequestEvent(this, List.of("source")));
        listener.refreshDocument("marker", () -> {
            deleted.countDown();
            return Mono.never();
        });
        assertThat(deleted.await(3, TimeUnit.SECONDS)).isTrue();
        assertThat(total("sourcekeyword", null)).isZero();
        assertThat(attempts).hasValue(3);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 101})
    void invalidVariantGroupsDoNotRetry(int count) {
        var attempts = new AtomicInteger();
        HaloDocumentVariantProvider provider = source -> {
            attempts.incrementAndGet();
            return Flux.range(0, count).map(index -> variant());
        };
        when(extensions.getEnabledExtension(HaloDocumentVariantProvider.class)).thenReturn(Mono.just(provider));

        index();

        assertThat(attempts).hasValue(1);
        assertThat(total("sourcekeyword", null)).isEqualTo(1);
        assertThat(total("variantkeyword", "en")).isZero();
    }

    private void index() {
        listener.indexDocuments(Flux.just(document()), engine).block(Duration.ofSeconds(5));
    }

    private long total(String keyword, String locale) {
        var option = new SearchOption();
        option.setKeyword(keyword);
        option.setLocale(locale);
        option.setFilterPublished(true);
        option.setFilterExposed(true);
        option.setFilterRecycled(false);
        return engine.search(option).getTotal();
    }

    private static HaloDocumentVariantProvider.Variant variant() {
        return new HaloDocumentVariantProvider.Variant("en", "variantkeyword", "", "variantkeyword", "/en/source");
    }

    private static HaloDocument document() {
        var document = new HaloDocument();
        document.setId("source");
        document.setMetadataName("source");
        document.setTitle("sourcekeyword");
        document.setContent("sourcekeyword");
        document.setType("post.content.halo.run");
        document.setOwnerName("admin");
        document.setPermalink("/source");
        document.setCreationTimestamp(Instant.EPOCH);
        document.setPublished(true);
        document.setExposed(true);
        return document;
    }
}
