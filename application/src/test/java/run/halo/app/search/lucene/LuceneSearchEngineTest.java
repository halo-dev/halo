package run.halo.app.search.lucene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.store.Directory;
import org.assertj.core.util.Streams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.search.HaloDocument;
import run.halo.app.search.SearchOption;

@ExtendWith(MockitoExtension.class)
class LuceneSearchEngineTest {

    @Mock
    IndexWriter indexWriter;

    @Mock
    SearcherManager searcherManager;

    @Mock
    Directory directory;

    @Mock
    Analyzer analyzer;

    LuceneSearchEngine searchEngine;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        var searchEngine = new LuceneSearchEngine(tempDir);
        searchEngine.setIndexWriter(indexWriter);
        searchEngine.setDirectory(directory);
        searchEngine.setSearcherManager(searcherManager);
        searchEngine.setAnalyzer(analyzer);
        this.searchEngine = searchEngine;
    }


    @Test
    void shouldAddOrUpdateDocument() throws IOException {
        var haloDoc = createFakeHaloDoc();
        searchEngine.addOrUpdate(List.of(haloDoc));
        verify(this.indexWriter).updateDocuments(any(Query.class), assertArg(docs -> {
            var docList = Streams.stream(docs).toList();
            assertEquals(1, docList.size());
            var doc = docList.get(0);
            assertInstanceOf(Document.class, doc);
            var document = (Document) doc;
            assertEquals("fake-id", document.get("id"));
        }));
        verify(this.searcherManager).maybeRefreshBlocking();
        verify(this.indexWriter).commit();
    }

    @Test
    void shouldDeleteDocument() throws IOException {
        this.searchEngine.deleteDocument(List.of("fake-id"));
        verify(this.indexWriter).deleteDocuments(any(Query.class));
        verify(this.searcherManager).maybeRefreshBlocking();
        verify(this.indexWriter).commit();
    }

    @Test
    void shouldDeleteAll() throws IOException {
        this.searchEngine.deleteAll();

        verify(this.indexWriter).deleteAll();
        verify(this.searcherManager).maybeRefreshBlocking();
        verify(this.indexWriter).commit();
    }

    @Test
    void shouldDestroy() throws Exception {
        this.searchEngine.destroy();
        verify(this.analyzer).close();
        verify(this.searcherManager).close();
        verify(this.indexWriter).close();
        verify(this.directory).close();
    }

    @Test
    void shouldAlwaysDestroyAllEvenErrorOccurred() throws Exception {
        var analyzerCloseError = new IOException("analyzer close error");
        doThrow(analyzerCloseError).when(this.analyzer).close();

        var directoryCloseError = new IOException("directory close error");
        doThrow(directoryCloseError).when(this.directory).close();
        var e = assertThrows(IOException.class, () -> this.searchEngine.destroy());
        assertEquals(analyzerCloseError, e);
        assertEquals(directoryCloseError, e.getSuppressed()[0]);
        verify(this.analyzer).close();
        verify(this.searcherManager).close();
        verify(this.indexWriter).close();
        verify(this.directory).close();
    }

    @Test
    void shouldSearch() throws IOException {
        var searcher = mock(IndexSearcher.class);
        when(this.searcherManager.acquire()).thenReturn(searcher);
        this.searchEngine.setAnalyzer(new StandardAnalyzer());

        var totalHits = new TotalHits(1234, TotalHits.Relation.EQUAL_TO);
        var scoreDoc = new ScoreDoc(1, 1.0f);

        var topFieldDocs = new TopFieldDocs(totalHits, new ScoreDoc[] {scoreDoc}, null);
        when(searcher.search(any(Query.class), eq(123), any(Sort.class)))
            .thenReturn(topFieldDocs);
        var storedFields = mock(StoredFields.class);

        var haloDoc = createFakeHaloDoc();
        var doc = this.searchEngine.getHaloDocumentConverter().convert(haloDoc);
        when(storedFields.document(1)).thenReturn(doc);
        when(searcher.storedFields()).thenReturn(storedFields);

        var option = new SearchOption();
        option.setKeyword("fake");
        option.setLimit(123);
        option.setHighlightPreTag("<fake-tag>");
        option.setHighlightPostTag("</fake-tag>");
        var result = this.searchEngine.search(option);
        assertEquals(1234, result.getTotal());
        assertEquals("fake", result.getKeyword());
        assertEquals(123, result.getLimit());
        assertEquals(1, result.getHits().size());
        var gotHaloDoc = result.getHits().get(0);
        assertEquals("fake-id", gotHaloDoc.getId());
        assertEquals("<fake-tag>fake</fake-tag>-title", gotHaloDoc.getTitle());
        assertNull(gotHaloDoc.getDescription());
        assertEquals("<fake-tag>fake</fake-tag>-content", gotHaloDoc.getContent());
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
