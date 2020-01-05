package run.halo.app.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import run.halo.app.model.entity.Post;
import run.halo.app.repository.base.BasePostRepository;


/**
 * Post repository.
 *
 * @author johnniang
 * @author ryanwang
 */
public interface PostRepository extends BasePostRepository<Post>, JpaSpecificationExecutor<Post> {

    /**
     * Count all post visits.
     *
     * @return visits.
     */
    @Override
    @Query("select sum(p.visits) from Post p")
    Long countVisit();

    /**
     * Count all post likes.
     *
     * @return likes.
     */
    @Override
    @Query("select sum(p.likes) from Post p")
    Long countLike();

}
