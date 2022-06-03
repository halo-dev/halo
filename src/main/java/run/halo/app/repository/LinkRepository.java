package run.halo.app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import run.halo.app.model.entity.Link;
import run.halo.app.repository.base.BaseRepository;

/**
 * Link repository.
 *
 * @author johnniang
 */
public interface LinkRepository extends BaseRepository<Link, Integer> {

    /**
     * Find all link teams.
     *
     * @return a list of teams
     */
    @Query(value = "select a.team from Link a group by a.team order by max(a.priority) DESC")
    List<String> findAllTeams();

    boolean existsByNameAndIdNot(String name, Integer id);

    boolean existsByUrlAndIdNot(String url, Integer id);
}
