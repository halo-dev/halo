package run.halo.app.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.search.event.HaloDocumentDeleteRequestEvent;
import run.halo.app.search.event.HaloDocumentRebuildRequestEvent;
import run.halo.app.search.lucene.LuceneSearchEngine;

class SearchIndexWriteRecoveryTest {

    private static final Duration WRITE_TIMEOUT = Duration.ofMillis(250);

    @TempDir
    Path directory;

    @Test
    void timedOutRebuildWriteCannotContinueBatchesOrRestoreDeletedSourceAndVariant() throws Exception {
        var engine = spy(new LuceneSearchEngine(directory));
        engine.afterPropertiesSet();
        var extensions = mock(ExtensionGetter.class);
        var listener = listener(extensions);
        var entered = new CountDownLatch(1);
        var interrupted = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var drained = new CountDownLatch(1);
        var batches = new CopyOnWriteArrayList<List<String>>();
        try {
            listener.setBufferSize(1);
            when(extensions.getEnabledExtension(SearchEngine.class)).thenReturn(Mono.just(engine));
            when(extensions.getEnabledExtension(HaloDocumentVariantProvider.class))
                    .thenReturn(Mono.just(source -> Flux.just(new HaloDocumentVariantProvider.Variant(
                            "en", "translatedkeyword", null, "translatedkeyword", "/en/" + source.getId()))));
            var documents = mock(HaloDocumentsProvider.class);
            when(documents.fetchAll()).thenReturn(Flux.just(document("first"), document("second")));
            when(extensions.getExtensions(HaloDocumentsProvider.class)).thenReturn(Flux.just(documents));
            doAnswer(call -> {
                        Iterable<HaloDocument> items = call.getArgument(0);
                        var ids = new ArrayList<String>();
                        items.forEach(item -> ids.add(item.getId()));
                        batches.add(ids);
                        entered.countDown();
                        awaitIgnoringInterruption(release, interrupted);
                        return call.callRealMethod();
                    })
                    .when(engine)
                    .addOrUpdate(any());

            listener.onApplicationEvent(new HaloDocumentRebuildRequestEvent(this));
            assertThat(entered.await(3, TimeUnit.SECONDS)).isTrue();
            listener.onApplicationEvent(new HaloDocumentDeleteRequestEvent(this, List.of("first")));
            listener.refreshDocument("queue-marker", () -> {
                drained.countDown();
                return Mono.empty();
            });

            assertThat(interrupted.await(3, TimeUnit.SECONDS)).isTrue();
            assertThat(drained.await(WRITE_TIMEOUT.toMillis() * 3, TimeUnit.MILLISECONDS))
                    .isFalse();
            verify(engine, never()).deleteDocument(List.of("first"));

            release.countDown();
            assertThat(drained.await(3, TimeUnit.SECONDS)).isTrue();
            assertThat(batches).containsExactly(List.of("first", "first:locale:en"));
            assertThat(total(engine, "sourcekeyword", null)).isZero();
            assertThat(total(engine, "translatedkeyword", "en")).isZero();
            verify(engine).deleteDocument(List.of("first"));
        } finally {
            release.countDown();
            listener.shutdown();
            engine.destroy();
        }
    }

    @Test
    void interruptedRebuildStopsBeforeReadingDocumentsAndProcessesQueuedDeletion() throws Exception {
        var engine = mock(SearchEngine.class);
        var extensions = mock(ExtensionGetter.class);
        var listener = listener(extensions);
        var entered = new CountDownLatch(1);
        var interrupted = new CountDownLatch(1);
        var deleted = new CountDownLatch(1);
        try {
            when(extensions.getEnabledExtension(SearchEngine.class)).thenReturn(Mono.just(engine));
            when(engine.available()).thenReturn(true);
            doAnswer(call -> {
                        entered.countDown();
                        try {
                            new CountDownLatch(1).await();
                        } catch (InterruptedException error) {
                            interrupted.countDown();
                            Thread.currentThread().interrupt();
                        }
                        return null;
                    })
                    .when(engine)
                    .deleteAll();
            doAnswer(call -> {
                        deleted.countDown();
                        return null;
                    })
                    .when(engine)
                    .deleteDocument(List.of("post"));

            listener.onApplicationEvent(new HaloDocumentRebuildRequestEvent(this));
            assertThat(entered.await(3, TimeUnit.SECONDS)).isTrue();
            listener.onApplicationEvent(new HaloDocumentDeleteRequestEvent(this, List.of("post")));

            assertThat(interrupted.await(3, TimeUnit.SECONDS)).isTrue();
            assertThat(deleted.await(3, TimeUnit.SECONDS)).isTrue();
            verify(extensions, never()).getExtensions(HaloDocumentsProvider.class);
            verify(engine, never()).addOrUpdate(any());
        } finally {
            listener.shutdown();
        }
    }

    private static HaloDocumentEventsListener listener(ExtensionGetter extensions) {
        var listener = new HaloDocumentEventsListener(extensions);
        var original = (SearchIndexWriter) ReflectionTestUtils.getField(listener, "indexWriter");
        assertThat(original).isNotNull();
        original.close();
        ReflectionTestUtils.setField(listener, "indexWriter", new SearchIndexWriter(WRITE_TIMEOUT));
        return listener;
    }

    private static long total(SearchEngine engine, String keyword, String locale) {
        var option = new SearchOption();
        option.setKeyword(keyword);
        option.setLocale(locale);
        option.setFilterPublished(true);
        option.setFilterExposed(true);
        return engine.search(option).getTotal();
    }

    private static HaloDocument document(String id) {
        var document = new HaloDocument();
        document.setId(id);
        document.setMetadataName(id);
        document.setType("post.content.halo.run");
        document.setTitle("sourcekeyword");
        document.setContent("sourcekeyword");
        document.setOwnerName("admin");
        document.setCreationTimestamp(Instant.EPOCH);
        document.setPermalink("/archives/" + id);
        document.setPublished(true);
        document.setExposed(true);
        return document;
    }

    private static void awaitIgnoringInterruption(CountDownLatch release, CountDownLatch interrupted) {
        while (true) {
            try {
                release.await();
                return;
            } catch (InterruptedException error) {
                interrupted.countDown();
            }
        }
    }
}
