package run.halo.app.search;

import org.pf4j.ExtensionPoint;

/**
 * Search engine is used to index and search halo documents. Meanwhile, it is also an extension
 * point for adding different search engine implementations.
 *
 * @author johnniang
 */
public interface SearchEngine extends ExtensionPoint {

    /**
     * Whether the search engine is available.
     *
     * @return true if available, false otherwise
     */
    boolean available();

    /**
     * Add or update halo documents.
     *
     * @param haloDocuments halo documents
     */
    void addOrUpdate(Iterable<HaloDocument> haloDocuments);

    /**
     * Delete halo documents by ids.
     *
     * @param haloDocIds halo document ids
     */
    void deleteDocument(Iterable<String> haloDocIds);

    /**
     * Delete all halo documents.
     */
    void deleteAll();

    /**
     * Search halo documents.
     *
     * @param option search option
     * @return search result of halo documents
     */
    SearchResult search(SearchOption option);

}
