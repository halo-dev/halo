package run.halo.app.extension.store;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * An interface to query and operate ExtensionStore.
 *
 * @author johnniang
 */
public interface ExtensionStoreClient {

    /**
     * Lists all ExtensionStores by name prefix.
     *
     * @param prefix is the prefix of ExtensionStore name.
     * @return all ExtensionStores which names start with the prefix.
     */
    List<Extensions> listByNamePrefix(String prefix);

    Page<Extensions> listByNamePrefix(String prefix, Pageable pageable);

    /**
     * Lists ExtensionStores by name prefix, after the given cursor name, and limit the result size.
     *
     * @param prefix the name prefix
     * @param nameCursor cursor name, exclusive and can be null
     * @param limit the max result size
     * @return a list of extension stores
     */
    List<Extensions> listBy(String prefix, String nameCursor, int limit);

    List<Extensions> listByNames(List<String> names);

    /**
     * Fetches an ExtensionStore by unique name.
     *
     * @param name is the full name of an ExtensionStore.
     * @return an optional ExtensionStore.
     */
    Optional<Extensions> fetchByName(String name);

    /**
     * Creates an ExtensionStore.
     *
     * @param name is the full name of an ExtensionStore.
     * @param data is Extension body to be persisted.
     * @return a fresh ExtensionStore created just now.
     */
    Extensions create(String name, byte[] data);

    /**
     * Updates an ExtensionStore with version to prevent concurrent update.
     *
     * @param name is the full name of an ExtensionStore.
     * @param version is the expected version of ExtensionStore.
     * @param data is Extension body to be updated.
     * @return updated ExtensionStore with a fresh version.
     */
    Extensions update(String name, Long version, byte[] data);

    /**
     * Deletes an ExtensionStore by name and current version.
     *
     * @param name is the full name of an ExtensionStore.
     * @param version is the expected version of ExtensionStore.
     * @return previous ExtensionStore.
     */
    Extensions delete(String name, Long version);

    //TODO add watch method here.
}
