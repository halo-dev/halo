package run.halo.app.search;

import org.pf4j.ExtensionPoint;
import reactor.core.publisher.Flux;

/**
 * Halo documents provider. This interface is used to rebuild the search index.
 *
 * @author johnniang
 */
public interface HaloDocumentsProvider extends ExtensionPoint {

    /**
     * Fetch all halo documents.
     *
     * @return all halo documents
     */
    Flux<HaloDocument> fetchAll();

    /**
     * Get type of documents.
     *
     * @return type of documents
     */
    String getType();

}
