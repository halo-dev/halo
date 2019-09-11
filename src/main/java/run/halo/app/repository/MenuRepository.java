package run.halo.app.repository;

import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.Menu;
import run.halo.app.repository.base.BaseRepository;

import java.util.List;

/**
 * Menu repository.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-8-28
 */
public interface MenuRepository extends BaseRepository<Menu, Integer> {

    boolean existsByName(@NonNull String name);

    boolean existsByIdNotAndName(@NonNull Integer id, @NonNull String name);

    List<Menu> findByParentId(@NonNull Integer id);

    List<Menu> findByTeam(String team, Sort sort);
}
