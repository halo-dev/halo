package run.halo.app.search.post;

import static org.apache.commons.lang3.StringUtils.stripToEmpty;
import static org.apache.lucene.document.Field.Store.NO;
import static org.apache.lucene.document.Field.Store.YES;
import static org.apache.lucene.index.IndexWriterConfig.OpenMode.APPEND;
import static org.apache.lucene.index.IndexWriterConfig.OpenMode.CREATE_OR_APPEND;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.wltea.analyzer.lucene.IKAnalyzer;
import reactor.core.Exceptions;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.search.SearchParam;
import run.halo.app.search.SearchResult;

@Service
@Slf4j
public class LucenePostSearchService implements PostSearchService, DisposableBean {

    public static final int MAX_FRAGMENT_SIZE = 100;

    private final Analyzer analyzer;

    private final Directory postIndexDir;

    public LucenePostSearchService(HaloProperties haloProperties)
        throws IOException {
        analyzer = new IKAnalyzer(true);
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
        var highlighter = new Highlighter(
            new SimpleHTMLFormatter(param.getHighlightPreTag(), param.getHighlightPostTag()),
            new QueryScorer(query));
        highlighter.setTextFragmenter(new SimpleFragmenter(MAX_FRAGMENT_SIZE));

        var hits = new ArrayList<PostHit>(topDocs.scoreDocs.length);
        for (var scoreDoc : topDocs.scoreDocs) {
            hits.add(convert(searcher.storedFields().document(scoreDoc.doc), highlighter));
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
        writeConfig.setOpenMode(APPEND);
        try (var writer = new IndexWriter(postIndexDir, writeConfig)) {
            var terms = postNames.stream()
                .map(postName -> new Term(PostDoc.ID_FIELD, postName))
                .toArray(Term[]::new);
            long seqNum = writer.deleteDocuments(terms);
            log.debug("Deleted documents({}) with sequence number {}", terms.length, seqNum);
        }
    }

    @Override
    public void destroy() throws Exception {
        analyzer.close();
        postIndexDir.close();
    }


    private Query buildQuery(String keyword) throws ParseException {
        if (log.isDebugEnabled()) {
            log.debug("Trying to search for keyword: {}", keyword);
        }
        return new QueryParser("searchable", analyzer).parse(keyword);
    }

    private Document convert(PostDoc post) {
        var doc = new Document();
        doc.add(new StringField("name", post.name(), YES));
        doc.add(new StoredField("title", post.title()));

        var content = Jsoup.clean(stripToEmpty(post.excerpt()) + stripToEmpty(post.content()),
            Safelist.none());

        doc.add(new StoredField("content", content));
        doc.add(new TextField("searchable", post.title() + content, NO));

        long publishTimestamp = post.publishTimestamp().toEpochMilli();
        doc.add(new LongPoint("publishTimestamp", publishTimestamp));
        doc.add(new StoredField("publishTimestamp", publishTimestamp));
        doc.add(new StoredField("permalink", post.permalink()));
        return doc;
    }

    private PostHit convert(Document doc, Highlighter highlighter)
        throws IOException, InvalidTokenOffsetsException {
        var post = new PostHit();
        post.setName(doc.get("name"));

        var title = getHighlightedText(doc, "title", highlighter, MAX_FRAGMENT_SIZE);
        post.setTitle(title);

        var content = getHighlightedText(doc, "content", highlighter, MAX_FRAGMENT_SIZE);
        post.setContent(content);

        var publishTimestamp = doc.getField("publishTimestamp").numericValue().longValue();
        post.setPublishTimestamp(Instant.ofEpochMilli(publishTimestamp));
        post.setPermalink(doc.get("permalink"));
        return post;
    }

    private String getHighlightedText(Document doc, String field, Highlighter highlighter,
        int maxLength)
        throws InvalidTokenOffsetsException, IOException {
        try {
            var highlightedText = highlighter.getBestFragment(analyzer, field, doc.get(field));
            if (highlightedText != null) {
                return highlightedText;
            }
        } catch (IllegalArgumentException iae) {
            // TODO we have to ignore the error currently due to no solution about the error.
            if (!"boost must be a positive float, got -1.0".equals(iae.getMessage())) {
                throw iae;
            }
        }
        // handle if there is not highlighted text
        var fieldValue = doc.get(field);
        return StringUtils.substring(fieldValue, 0, maxLength);
    }
}
