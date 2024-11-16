package run.halo.app.search.lucene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.AlreadyClosedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.search.HaloDocument;
import run.halo.app.search.SearchOption;

@ExtendWith(MockitoExtension.class)
class LuceneSearchEngineTest {

    LuceneSearchEngine searchEngine;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        this.searchEngine = new LuceneSearchEngine(tempDir);
        this.searchEngine.afterPropertiesSet();
    }

    @AfterEach
    void cleanUp() throws Exception {
        this.searchEngine.destroy();
    }

    @Test
    void shouldAddOrUpdateDocument() throws IOException {
        var haloDoc = createFakeHaloDoc();
        searchEngine.addOrUpdate(List.of(haloDoc));
        // validate the index
        var reader = DirectoryReader.open(searchEngine.getDirectory());
        assertEquals(1, reader.getDocCount("id"));
    }

    @Test
    void shouldDeleteDocument() throws IOException {
        var haloDoc = createFakeHaloDoc();
        searchEngine.addOrUpdate(List.of(haloDoc));

        var reader = DirectoryReader.open(searchEngine.getDirectory());
        assertEquals(1, reader.getDocCount("id"));

        this.searchEngine.deleteDocument(List.of("fake-id"));

        reader = DirectoryReader.open(searchEngine.getDirectory());
        assertEquals(0, reader.getDocCount("id"));
    }

    @Test
    void shouldDeleteAll() throws IOException {
        var haloDoc = createFakeHaloDoc();
        searchEngine.addOrUpdate(List.of(haloDoc));

        var reader = DirectoryReader.open(searchEngine.getDirectory());
        assertEquals(1, reader.getDocCount("id"));

        this.searchEngine.deleteAll();

        reader = DirectoryReader.open(searchEngine.getDirectory());
        assertEquals(0, reader.getDocCount("id"));
    }

    @Test
    void shouldAddOrUpdateDocumentConcurrently()
        throws ExecutionException, InterruptedException, TimeoutException {
        runConcurrently(() -> {
            var haloDoc = createFakeHaloDoc();
            searchEngine.addOrUpdate(List.of(haloDoc));
        });
    }

    @Test
    void shouldDeleteDocumentConcurrently()
        throws ExecutionException, InterruptedException, TimeoutException {
        runConcurrently(() -> {
            var haloDoc = createFakeHaloDoc();
            searchEngine.addOrUpdate(List.of(haloDoc));
            searchEngine.deleteDocument(List.of(haloDoc.getId()));
        });
    }

    @Test
    void shouldDeleteAllConcurrently()
        throws ExecutionException, InterruptedException, TimeoutException {
        runConcurrently(() -> {
            var haloDoc = createFakeHaloDoc();
            searchEngine.addOrUpdate(List.of(haloDoc));
            searchEngine.deleteAll();
        });
    }

    @Test
    void shouldDestroy() throws Exception {
        var directory = this.searchEngine.getDirectory();
        this.searchEngine.destroy();
        assertThrows(AlreadyClosedException.class, () -> DirectoryReader.open(directory));
    }

    @Test
    void shouldSearchNothingIfIndexNotFound() {
        var option = new SearchOption();
        option.setKeyword("fake");
        option.setLimit(123);
        option.setHighlightPreTag("<fake-tag>");
        option.setHighlightPostTag("</fake-tag>");
        var result = this.searchEngine.search(option);
        assertEquals(0, result.getTotal());
        assertEquals("fake", result.getKeyword());
        assertEquals(123, result.getLimit());
        assertEquals(0, result.getHits().size());
    }

    @Test
    void shouldSearch() {
        this.searchEngine.addOrUpdate(List.of(createFakeHaloDoc()));

        var option = new SearchOption();
        option.setKeyword("fake");
        option.setLimit(123);
        option.setHighlightPreTag("<fake-tag>");
        option.setHighlightPostTag("</fake-tag>");
        var result = this.searchEngine.search(option);
        assertEquals(1, result.getTotal());
        assertEquals("fake", result.getKeyword());
        assertEquals(123, result.getLimit());
        assertEquals(1, result.getHits().size());
        var gotHaloDoc = result.getHits().get(0);
        assertEquals("fake-id", gotHaloDoc.getId());
        assertEquals("<fake-tag>fake</fake-tag>-title", gotHaloDoc.getTitle());
        assertNull(gotHaloDoc.getDescription());
        assertEquals("<fake-tag>fake</fake-tag>-content", gotHaloDoc.getContent());
    }

    void runConcurrently(Runnable runnable)
        throws ExecutionException, InterruptedException, TimeoutException {
        var executorService = Executors.newFixedThreadPool(10);
        var futures = IntStream.of(0, 10)
            .mapToObj(i -> CompletableFuture.runAsync(runnable, executorService))
            .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).get(10, TimeUnit.SECONDS);
        executorService.shutdownNow();
        assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));
    }

    HaloDocument createFakeHaloDoc() {
        var haloDoc = new HaloDocument();
        haloDoc.setId("fake-id");
        haloDoc.setMetadataName("fake-name");
        haloDoc.setTitle("fake-title");
        haloDoc.setDescription(null);
        haloDoc.setContent("fake-content");
        haloDoc.setType("fake-type");
        haloDoc.setOwnerName("fake-owner");
        var now = Instant.now();
        haloDoc.setCreationTimestamp(now);
        haloDoc.setUpdateTimestamp(null);
        haloDoc.setPermalink("/fake-permalink");
        haloDoc.setAnnotations(Map.of("fake-anno-key", "fake-anno-value"));
        return haloDoc;
    }

}
