package run.halo.app.repository;

import run.halo.app.model.entity.Menu;
import run.halo.app.repository.base.BaseRepository;
import org.springframework.lang.NonNull;
import run.halo.app.repository.base.BaseRepository;

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
