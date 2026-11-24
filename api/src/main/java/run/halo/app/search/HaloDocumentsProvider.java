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
     * <p>Providers for Extension resources should set {@link HaloDocument#getResource()} from the source extension.
     * Providers for non-Extension data should use {@link run.halo.app.localization.ResourceReference#external(String,
     * String, String)}. Localized variant providers use that stable identity instead of inferring a resource type from
     * {@link HaloDocument#getType()}.
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
