package run.halo.app.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import run.halo.app.model.entity.Photo;
import run.halo.app.repository.base.BaseRepository;

import java.util.List;

/**
 * Photo repository.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-03
 */
public interface PhotoRepository extends BaseRepository<Photo, Integer>, JpaSpecificationExecutor<Photo> {

    /**
     * Query photos by team
     *
     * @param team team
     * @param sort sort
     * @return list of photo
     */
    List<Photo> findByTeam(String team, Sort sort);

    /**
     * Find all photo teams.
     *
     * @return list of teams.
     */
    @Query(value = "select distinct p.team from Photo p")
    List<String> findAllTeams();
}
