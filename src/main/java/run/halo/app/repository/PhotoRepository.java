package run.halo.app.repository;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import run.halo.app.model.entity.Photo;
import run.halo.app.repository.base.BaseRepository;

/**
 * Photo repository.
 *
 * @author johnniang
 * @author ryanwang
 * @author guqing
 * @date 2019-04-03
 */
public interface PhotoRepository
    extends BaseRepository<Photo, Integer>, JpaSpecificationExecutor<Photo> {

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

    /**
     * Updates photo likes.
     *
     * @param likes likes delta
     * @param photoId photo id must not be null
     * @return updated rows
     */
    @Modifying
    @Query("update Photo p set p.likes = p.likes + :likes where p.id = :photoId")
    int updateLikes(long likes, Integer photoId);
}
