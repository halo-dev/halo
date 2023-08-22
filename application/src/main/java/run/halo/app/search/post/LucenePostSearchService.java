package run.halo.app.search.post;

import static org.apache.lucene.document.Field.Store.YES;
import static org.apache.lucene.index.IndexWriterConfig.OpenMode.CREATE_OR_APPEND;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilterFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.PointsConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.highlight.DefaultEncoder;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import reactor.core.Exceptions;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.search.SearchParam;
import run.halo.app.search.SearchResult;

@Service
@Slf4j
public class LucenePostSearchService implements PostSearchService, DisposableBean {

    private final Analyzer analyzer;

    private final Directory postIndexDir;

    public LucenePostSearchService(HaloProperties haloProperties)
        throws IOException {
        analyzer = CustomAnalyzer.builder()
            .withTokenizer(StandardTokenizerFactory.class)
            .addCharFilter(HTMLStripCharFilterFactory.NAME)
            .addTokenFilter(LowerCaseFilterFactory.NAME)
            .build();

        var postIdxPath = haloProperties.getWorkDir().resolve("indices/posts");
        postIndexDir = FSDirectory.open(postIdxPath);
    }

    @Override
    public SearchResult<PostHit> search(SearchParam param) throws Exception {
        var dirReader = DirectoryReader.open(postIndexDir);
        var searcher = new IndexSearcher(dirReader);
        var keyword = param.getKeyword();
        var watch = new StopWatch("SearchWatch");
        watch.start("search for " + keyword);
        var query = buildQuery(keyword);
        var topDocs = searcher.search(query, param.getLimit(), Sort.RELEVANCE);
        watch.stop();

        var formatter =
            new SimpleHTMLFormatter(param.getHighlightPreTag(), param.getHighlightPostTag());
        var scorer = new QueryScorer(query);
        var highlighter = new Highlighter(formatter, new DefaultEncoder(), scorer);
        var hits = new ArrayList<PostHit>(topDocs.scoreDocs.length);
        for (var scoreDoc : topDocs.scoreDocs) {
            var doc = searcher.storedFields().document(scoreDoc.doc);

            var title = doc.get("title");
            var titleFragment = highlighter.getBestFragment(analyzer, "title", title);
            if (titleFragment != null) {
                title = titleFragment;
            }

            var content = doc.get("content");
            var contentFragment = highlighter.getBestFragment(analyzer, "content", content);
            if (contentFragment != null) {
                content = contentFragment;
            }

            var post = new PostHit();
            post.setName(doc.get("name"));
            post.setTitle(title);
            post.setContent(content);
            var publishTimestamp = doc.getField("publishTimestamp").numericValue().longValue();
            post.setPublishTimestamp(Instant.ofEpochSecond(publishTimestamp));
            post.setPermalink(doc.get("permalink"));
            hits.add(post);
        }

        var result = new SearchResult<PostHit>();
        result.setHits(hits);
        result.setTotal(topDocs.totalHits.value);
        result.setKeyword(param.getKeyword());
        result.setLimit(param.getLimit());
        result.setProcessingTimeMillis(watch.getTotalTimeMillis());
        return result;
    }

    @Override
    public void addDocuments(List<PostDoc> posts) throws IOException {
        var writeConfig = new IndexWriterConfig(analyzer);
        writeConfig.setOpenMode(CREATE_OR_APPEND);
        try (var writer = new IndexWriter(postIndexDir, writeConfig)) {
            posts.forEach(post -> {
                var doc = this.convert(post);
                try {
                    var seqNum =
                        writer.updateDocument(new Term(PostDoc.ID_FIELD, post.name()), doc);
                    if (log.isDebugEnabled()) {
                        log.debug("Updated document({}) with sequence number {} returned",
                            post.name(), seqNum);
                    }
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            });
        }
    }

    @Override
    public void removeDocuments(Set<String> postNames) throws IOException {
        var writeConfig = new IndexWriterConfig(analyzer);
        writeConfig.setOpenMode(CREATE_OR_APPEND);
        try (var writer = new IndexWriter(postIndexDir, writeConfig)) {
            var terms = postNames.stream()
                .map(postName -> new Term(PostDoc.ID_FIELD, postName))
                .toArray(Term[]::new);
            long seqNum = writer.deleteDocuments(terms);
            log.debug("Deleted documents({}) with sequence number {}", terms.length, seqNum);
        }
    }

    @Override
    public void removeAllDocuments() throws Exception {
        var writeConfig = new IndexWriterConfig(analyzer);
        writeConfig.setOpenMode(CREATE_OR_APPEND);
        try (var writer = new IndexWriter(postIndexDir, writeConfig)) {
            writer.deleteAll();
        }
    }

    @Override
    public void destroy() throws Exception {
        analyzer.close();
        postIndexDir.close();
    }


    private Query buildQuery(String keyword) throws QueryNodeException {
        if (log.isDebugEnabled()) {
            log.debug("Trying to search for keyword: {}", keyword);
        }
        var parser = new StandardQueryParser(analyzer);
        parser.setPointsConfigMap(Map.of(
            "publishTimestamp", new PointsConfig(NumberFormat.getNumberInstance(), Long.class)
        ));
        return parser.parse(keyword, "content");
    }

    private Document convert(PostDoc post) {
        var doc = new Document();
        doc.add(new StringField("name", post.name(), YES));
        doc.add(new TextField("title", post.title(), YES));
        doc.add(new TextField("excerpt", post.excerpt(), YES));
        doc.add(new TextField("content", post.content(), YES));

        var publishTimestamp = post.publishTimestamp().getEpochSecond();
        doc.add(new LongPoint("publishTimestamp", publishTimestamp));
        doc.add(new StoredField("publishTimestamp", publishTimestamp));
        doc.add(new StoredField("permalink", post.permalink()));
        return doc;
    }

}
