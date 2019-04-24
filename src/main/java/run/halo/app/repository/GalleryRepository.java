package run.halo.app.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import run.halo.app.model.entity.Gallery;
import run.halo.app.repository.base.BaseRepository;

import java.util.List;

/**
 * Gallery repository.
 *
 * @author johnniang
 */
public interface GalleryRepository extends BaseRepository<Gallery, Integer>, JpaSpecificationExecutor<Gallery> {

    /**
     * Query galleries by team
     *
     * @param team team
     * @param sort sort
     * @return list of gallery
     */
    List<Gallery> findByTeam(String team, Sort sort);
}
