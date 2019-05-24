package run.halo.app.repository;

import org.springframework.lang.NonNull;
import run.halo.app.model.entity.Menu;
import run.halo.app.repository.base.BaseRepository;

/**
 * Menu repository.
 *
 * @author johnniang
 */
public interface MenuRepository extends BaseRepository<Menu, Integer> {

    boolean existsByName(@NonNull String name);

    boolean existsByIdNotAndName(@NonNull Integer id, @NonNull String name);
}
