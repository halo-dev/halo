package run.halo.app.repository;

import org.springframework.data.jpa.repository.Query;
import run.halo.app.model.entity.Link;
import run.halo.app.repository.base.BaseRepository;

import java.util.List;

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
    @Query(value = "select distinct a.team from Link a")
    List<String> findAllTeams();
}
