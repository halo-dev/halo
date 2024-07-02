package run.halo.app.search.lucene;

import static org.apache.lucene.document.Field.Store.YES;
import static org.apache.lucene.index.IndexWriterConfig.OpenMode.CREATE_OR_APPEND;
import static org.apache.lucene.search.BooleanClause.Occur.FILTER;
import static org.apache.lucene.search.BooleanClause.Occur.MUST;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilterFactory;
import org.apache.lucene.analysis.cjk.CJKBigramFilterFactory;
import org.apache.lucene.analysis.cjk.CJKWidthCharFilterFactory;
import org.apache.lucene.analysis.cjk.CJKWidthFilterFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermInSetQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryTermScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IOUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import run.halo.app.search.HaloDocument;
import run.halo.app.search.SearchEngine;
import run.halo.app.search.SearchOption;
import run.halo.app.search.SearchResult;

@Slf4j
public class LuceneSearchEngine implements SearchEngine, InitializingBean, DisposableBean {

    private final Path indexRootDir;

    private final Converter<HaloDocument, Document> haloDocumentConverter =
        new HaloDocumentConverter();

    private final Converter<Document, HaloDocument> documentConverter =
        new DocumentConverter();

    private Analyzer analyzer;

    private IndexWriter indexWriter;

    private SearcherManager searcherManager;

    private Directory directory;

    public LuceneSearchEngine(Path indexRootDir) throws IOException {
        this.indexRootDir = indexRootDir;
    }

    @Override
    public boolean available() {
        return true;
    }

    @Override
    public void addOrUpdate(Iterable<HaloDocument> haloDocs) {
        var docs = new LinkedList<Document>();
        var terms = new LinkedList<BytesRef>();
        haloDocs.forEach(haloDoc -> {
            var doc = this.haloDocumentConverter.convert(haloDoc);
            terms.add(new BytesRef(haloDoc.getId()));
            docs.add(doc);
        });
        var deleteQuery = new TermInSetQuery("id", terms);
        try {
            this.indexWriter.updateDocuments(deleteQuery, docs);
            this.searcherManager.maybeRefreshBlocking();
            this.indexWriter.commit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteDocument(Iterable<String> haloDocIds) {
        var terms = new LinkedList<BytesRef>();
        haloDocIds.forEach(haloDocId -> terms.add(new BytesRef(haloDocId)));
        var deleteQuery = new TermInSetQuery("id", terms);
        try {
            this.indexWriter.deleteDocuments(deleteQuery);
            this.searcherManager.maybeRefreshBlocking();
            this.indexWriter.commit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAll() {
        try {
            this.indexWriter.deleteAll();
            this.searcherManager.maybeRefreshBlocking();
            this.indexWriter.commit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SearchResult search(SearchOption option) {
        IndexSearcher searcher = null;
        try {
            searcher = searcherManager.acquire();
            var queryParser = new StandardQueryParser(analyzer);
            queryParser.setMultiFields(new String[] {"title", "description", "content"});
            queryParser.setFieldsBoost(Map.of("title", 1.0f, "description", 0.5f, "content", 0.2f));
            queryParser.setFuzzyMinSim(FuzzyQuery.defaultMaxEdits);
            queryParser.setFuzzyPrefixLength(FuzzyQuery.defaultPrefixLength);

            var keyword = option.getKeyword();
            var query = queryParser.parse(keyword, null);
            var queryBuilder = new BooleanQuery.Builder()
                .add(query, MUST);

            var filterExposed = option.getFilterExposed();
            if (filterExposed != null) {
                queryBuilder.add(
                    new TermQuery(new Term("exposed", filterExposed.toString())), FILTER
                );
            }
            var filterRecycled = option.getFilterRecycled();
            if (filterRecycled != null) {
                queryBuilder.add(
                    new TermQuery(new Term("recycled", filterRecycled.toString())), FILTER
                );
            }
            var filterPublished = option.getFilterPublished();
            if (filterPublished != null) {
                queryBuilder.add(
                    new TermQuery(new Term("published", filterPublished.toString())), FILTER
                );
            }
            var finalQuery = queryBuilder.build();
            var limit = option.getLimit();

            var stopWatch = new StopWatch("SearchWatch");
            stopWatch.start("search " + keyword);
            var hits = searcher.search(finalQuery, limit, Sort.RELEVANCE);
            stopWatch.stop();
            var formatter =
                new SimpleHTMLFormatter(option.getHighlightPreTag(), option.getHighlightPostTag());
            var queryScorer = new QueryTermScorer(query);
            var highlighter = new Highlighter(formatter, queryScorer);

            var haloDocs = new ArrayList<HaloDocument>(hits.scoreDocs.length);
            for (var hit : hits.scoreDocs) {
                var doc = searcher.storedFields().document(hit.doc);
                var haloDoc = documentConverter.convert(doc);

                var title = doc.get("title");
                var hlTitle = highlighter.getBestFragment(this.analyzer, "title", title);
                if (!StringUtils.hasText(hlTitle)) {
                    hlTitle = title;
                }

                var description = doc.get("description");
                var hlDescription =
                    highlighter.getBestFragment(this.analyzer, "description", description);

                var content = doc.get("content");
                var hlContent = highlighter.getBestFragment(this.analyzer, "content", content);

                haloDoc.setTitle(hlTitle);
                haloDoc.setDescription(hlDescription);
                haloDoc.setContent(hlContent);
                haloDocs.add(haloDoc);
            }
            var result = new SearchResult();
            result.setHits(haloDocs);
            result.setTotal(hits.totalHits.value);
            result.setKeyword(keyword);
            result.setLimit(limit);
            result.setProcessingTimeMillis(stopWatch.getTotalTimeMillis());
            return result;
        } catch (IOException | QueryNodeException | InvalidTokenOffsetsException e) {
            throw new RuntimeException(e);
        } finally {
            if (searcher != null) {
                try {
                    searcherManager.release(searcher);
                } catch (IOException e) {
                    log.error("Failed to release searcher", e);
                }
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            this.analyzer = CustomAnalyzer.builder()
                .withTokenizer(StandardTokenizerFactory.class)
                .addCharFilter(HTMLStripCharFilterFactory.NAME)
                .addCharFilter(CJKWidthCharFilterFactory.NAME)
                .addTokenFilter(LowerCaseFilterFactory.NAME)
                .addTokenFilter(CJKWidthFilterFactory.NAME)
                .addTokenFilter(CJKBigramFilterFactory.NAME)
                .build();
            this.directory = FSDirectory.open(this.indexRootDir);
            var writerConfig = new IndexWriterConfig(this.analyzer)
                .setOpenMode(CREATE_OR_APPEND);
            this.indexWriter = new IndexWriter(this.directory, writerConfig);
            this.searcherManager = new SearcherManager(this.indexWriter, null);
            log.info("Initialized lucene search engine");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void setIndexWriter(IndexWriter indexWriter) {
        this.indexWriter = indexWriter;
    }

    void setDirectory(Directory directory) {
        this.directory = directory;
    }

    void setSearcherManager(SearcherManager searcherManager) {
        this.searcherManager = searcherManager;
    }

    void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    Converter<HaloDocument, Document> getHaloDocumentConverter() {
        return haloDocumentConverter;
    }

    Converter<Document, HaloDocument> getDocumentConverter() {
        return documentConverter;
    }

    @Override
    public void destroy() throws Exception {
        var closers = new ArrayList<Closeable>(4);
        if (this.analyzer != null) {
            closers.add(this.analyzer);
        }
        if (this.searcherManager != null) {
            closers.add(this.searcherManager);
        }
        if (this.indexWriter != null) {
            closers.add(this.indexWriter);
        }
        if (this.directory != null) {
            closers.add(this.directory);
        }
        IOUtils.close(closers);
        log.info("Destroyed lucene search engine");
    }

    private static class HaloDocumentConverter implements Converter<HaloDocument, Document> {

        @Override
        @NonNull
        public Document convert(HaloDocument haloDoc) {
            var doc = new Document();
            doc.add(new StringField("id", haloDoc.getId(), YES));
            doc.add(new StringField("name", haloDoc.getMetadataName(), YES));
            doc.add(new StringField("type", haloDoc.getType(), YES));
            doc.add(new StringField("ownerName", haloDoc.getOwnerName(), YES));
            var categories = haloDoc.getCategories();
            if (categories != null) {
                categories.forEach(category -> doc.add(new StringField("category", category, YES)));
            }
            var tags = haloDoc.getTags();
            if (tags != null) {
                tags.forEach(tag -> doc.add(new StringField("tag", tag, YES)));
            }

            doc.add(new TextField("title", haloDoc.getTitle(), YES));
            doc.add(new TextField("description", haloDoc.getDescription(), YES));
            doc.add(new TextField("content", haloDoc.getContent(), YES));
            doc.add(new StringField("recycled", Boolean.toString(haloDoc.isRecycled()), YES));
            doc.add(new StringField("exposed", Boolean.toString(haloDoc.isExposed()), YES));
            doc.add(new StringField("published", Boolean.toString(haloDoc.isPublished()), YES));

            var annotations = haloDoc.getAnnotations();
            if (annotations != null) {
                try (var baos = new ByteArrayOutputStream();
                     var oos = new ObjectOutputStream(baos)) {
                    oos.writeObject(annotations);
                    var type = new FieldType();
                    type.setStored(true);
                    type.setTokenized(false);
                    type.setDocValuesType(DocValuesType.BINARY);
                    type.freeze();
                    doc.add(new StoredField("annotations", new BytesRef(baos.toByteArray()), type));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            var creationTimestamp = haloDoc.getCreationTimestamp();
            doc.add(new LongField("creationTimestamp", creationTimestamp.toEpochMilli(), YES));
            var updateTimestamp = haloDoc.getUpdateTimestamp();
            doc.add(new LongField("updateTimestamp", updateTimestamp.toEpochMilli(), YES));
            doc.add(new StringField("permalink", haloDoc.getPermalink(), YES));
            return doc;
        }
    }

    private static class DocumentConverter implements Converter<Document, HaloDocument> {

        @Override
        @NonNull
        public HaloDocument convert(Document doc) {
            var haloDoc = new HaloDocument();
            haloDoc.setId(doc.get("id"));
            haloDoc.setType(doc.get("type"));
            haloDoc.setMetadataName(doc.get("name"));
            haloDoc.setTitle(doc.get("title"));
            haloDoc.setDescription(doc.get("description"));
            haloDoc.setPermalink(doc.get("permalink"));
            haloDoc.setOwnerName(doc.get("ownerName"));
            haloDoc.setCategories(List.of(doc.getValues("category")));
            haloDoc.setTags(List.of(doc.getValues("tag")));

            haloDoc.setRecycled(getBooleanValue(doc, "recycled", false));
            haloDoc.setPublished(getBooleanValue(doc, "published", false));
            haloDoc.setExposed(getBooleanValue(doc, "exposed", false));

            var annotationsBytesRef = doc.getBinaryValue("annotations");
            if (annotationsBytesRef != null) {
                try (var bais = new ByteArrayInputStream(annotationsBytesRef.bytes);
                     var ois = new ObjectInputStream(bais)) {
                    @SuppressWarnings("unchecked")
                    var annotations = (Map<String, String>) ois.readObject();
                    haloDoc.setAnnotations(annotations);
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            var creationTimestamp = doc.getField("creationTimestamp").numericValue().longValue();
            haloDoc.setCreationTimestamp(Instant.ofEpochMilli(creationTimestamp));
            var updateTimestamp = doc.getField("updateTimestamp").numericValue().longValue();
            haloDoc.setUpdateTimestamp(Instant.ofEpochMilli(updateTimestamp));
            // handle content later
            return haloDoc;
        }

        private static boolean getBooleanValue(Document doc, String fieldName,
            boolean defaultValue) {
            var boolStr = doc.get(fieldName);
            return boolStr == null ? defaultValue : Boolean.parseBoolean(boolStr);
        }
    }
}
