package run.halo.app.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.search.event.HaloDocumentDeleteRequestEvent;
import run.halo.app.search.event.HaloDocumentRebuildRequestEvent;
import run.halo.app.search.lucene.LuceneSearchEngine;

class SearchIndexOrderingTest {

    @TempDir
    Path directory;

    @ParameterizedTest
    @CsvSource({"delete,false", "private,false", "unpublish,false", "delete,true", "private,true", "unpublish,true"})
    void oldRebuildCannotRestoreForbiddenHitAfterNewerChange(String change, boolean retry) throws Exception {
        var engine = new LuceneSearchEngine(directory);
        engine.afterPropertiesSet();
        var extensions = mock(ExtensionGetter.class);
        var listener = new HaloDocumentEventsListener(extensions);

        var resolving = new CountDownLatch(1);
        var release = Sinks.<HaloDocumentVariantProvider.Variant>one();
        var finished = new CountDownLatch(1);
        var loaded = new AtomicBoolean();
        var attempts = new AtomicInteger();
        var source = document();
        var variant = new HaloDocumentVariantProvider.Variant(
                "en", "variantkeyword", null, "variantkeyword", "/en/archives/post");
        try {
            when(extensions.getEnabledExtension(SearchEngine.class)).thenReturn(Mono.just(engine));
            var documents = mock(HaloDocumentsProvider.class);
            when(documents.fetchAll()).thenReturn(Flux.just(source));
            when(extensions.getExtensions(HaloDocumentsProvider.class)).thenReturn(Flux.just(documents));
            when(extensions.getEnabledExtension(HaloDocumentVariantProvider.class))
                    .thenReturn(Mono.just(document -> {
                        if (!document.isPublished() || !document.isExposed()) {
                            return Flux.empty();
                        }
                        int attempt = attempts.incrementAndGet();
                        if (retry && attempt == 1) {
                            return Flux.error(new IllegalStateException("Temporary variant failure"));
                        }
                        resolving.countDown();
                        return release.asMono().flux();
                    }));

            listener.onApplicationEvent(new HaloDocumentRebuildRequestEvent(this));
            assertThat(resolving.await(3, TimeUnit.SECONDS)).isTrue();

            if (change.equals("delete")) {
                listener.onApplicationEvent(new HaloDocumentDeleteRequestEvent(this, List.of(source.getId())));
            } else {
                listener.refreshDocument(source.getId(), () -> {
                    loaded.set(true);
                    var changed = document();
                    changed.setExposed(!change.equals("private"));
                    changed.setPublished(!change.equals("unpublish"));
                    return Mono.just(changed);
                });
            }
            listener.refreshDocument("queue-marker", () -> {
                finished.countDown();
                return Mono.never();
            });

            assertThat(loaded).isFalse();
            assertThat(finished.getCount()).isEqualTo(1);
            release.emitValue(variant, Sinks.EmitFailureHandler.FAIL_FAST);
            assertThat(finished.await(3, TimeUnit.SECONDS)).isTrue();
            if (!change.equals("delete")) {
                assertThat(loaded).isTrue();
            }

            var option = new SearchOption();
            option.setKeyword("variantkeyword");
            option.setLocale("en");
            assertThat(engine.search(option).getTotal()).isZero();
            assertThat(attempts).hasValue(retry ? 2 : 1);

            option.setKeyword("sourcekeyword");
            option.setLocale(null);
            option.setFilterPublished(true);
            option.setFilterExposed(true);
            assertThat(engine.search(option).getTotal()).isZero();
        } finally {
            release.tryEmitEmpty();
            listener.shutdown();
            engine.destroy();
        }
    }

    private static HaloDocument document() {
        var doc = new HaloDocument();
        doc.setId("post");
        doc.setMetadataName("post");
        doc.setType("post.content.halo.run");
        doc.setTitle("sourcekeyword");
        doc.setContent("sourcekeyword");
        doc.setOwnerName("admin");
        doc.setCreationTimestamp(Instant.EPOCH);
        doc.setPermalink("/archives/post");
        doc.setPublished(true);
        doc.setExposed(true);
        return doc;
    }
}
