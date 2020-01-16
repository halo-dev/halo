package run.halo.app.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
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

    /**
     * Query if the menu name already exists
     *
     * @param name name must not be null.
     * @return true or false
     */
    boolean existsByName(@NonNull String name);

    /**
     * Query if the menu name already exists by id and name.
     *
     * @param id   id must not be null.
     * @param name name must not be null.
     * @return true or false.
     */
    boolean existsByIdNotAndName(@NonNull Integer id, @NonNull String name);

    /**
     * Finds by menu parent id.
     *
     * @param id parent id must not be null.
     * @return a list of menu.
     */
    List<Menu> findByParentId(@NonNull Integer id);

    /**
     * Finds by menu team.
     *
     * @param team team must not be null.
     * @param sort sort.
     * @return a list of menu
     */
    List<Menu> findByTeam(@NonNull String team, Sort sort);

    /**
     * Find all menu teams.
     *
     * @return a list of teams
     */
    @Query(value = "select distinct a.team from Menu a")
    List<String> findAllTeams();
}
