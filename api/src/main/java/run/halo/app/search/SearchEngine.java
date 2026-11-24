package run.halo.app.search;

import org.pf4j.ExtensionPoint;

/**
 * Search engine is used to index and search halo documents. Meanwhile, it is also an extension point for adding
 * different search engine implementations.
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
     * Declares whether this engine implements the complete localized-document indexing contract.
     *
     * <p>Return true only when the engine stores {@link HaloDocument#getLocale()} and
     * {@link HaloDocument#getSourceId()}, filters queries to the requested locale, and excludes language variants when
     * {@link SearchOption#getLocale()} is null. Replacing or deleting a source document must also remove its existing
     * variants. An update may contain the source together with its replacement variants.
     *
     * <p>Core uses this capability to decide whether to request variants from {@link HaloDocumentVariantProvider} and
     * pass a content locale to {@link #search(SearchOption)}. The default is false, so existing engines continue to
     * index and search original documents without implementing language support.
     *
     * @return true when locale isolation and source/variant replacement and deletion are supported; false otherwise
     * @since 2.28.0
     */
    default boolean supportsLocalizedDocuments() {
        return false;
    }

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

    /** Delete all halo documents. */
    void deleteAll();

    /**
     * Search halo documents.
     *
     * @param option search option
     * @return search result of halo documents
     */
    SearchResult search(SearchOption option);
}
