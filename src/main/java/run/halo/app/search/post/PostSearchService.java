package run.halo.app.search.post;

import java.io.IOException;
import java.util.List;
import org.pf4j.ExtensionPoint;
import run.halo.app.search.SearchParam;
import run.halo.app.search.SearchResult;

public interface PostSearchService extends ExtensionPoint {

    SearchResult<PostHit> search(SearchParam searchParam) throws Exception;

    void addDocuments(List<PostDoc> posts) throws IOException;

    void removeDocuments(List<PostDoc> posts) throws IOException;

}
