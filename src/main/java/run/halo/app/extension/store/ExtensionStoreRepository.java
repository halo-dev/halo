package run.halo.app.extension.store;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * This repository contains some basic operations on ExtensionStore entity.
 *
 * @author johnniang
 */
@Repository
public interface ExtensionStoreRepository extends JpaRepository<ExtensionStore, String> {

    /**
     * Finds all ExtensionStore by name prefix.
     *
     * @param prefix is the prefix of name.
     * @return all ExtensionStores which names starts with the given prefix.
     */
    List<ExtensionStore> findAllByNameStartingWith(String prefix);

}
