package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.entity.Menu;
import cc.ryanc.halo.repository.base.BaseRepository;
import org.springframework.lang.NonNull;

/**
 * Menu repository.
 *
 * @author johnniang
 */
public interface MenuRepository extends BaseRepository<Menu, Integer> {

    /**
     * Exists by menu name.
     *
     * @param name must not be blank
     * @return true if exists; false otherwise
     */
    boolean existsByName(@NonNull String name);
}
