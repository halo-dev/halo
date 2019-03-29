package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.repository.base.BasePostRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;


/**
 * Post repository.
 *
 * @author johnniang
 * @author RYAN0UP
 */
public interface PostRepository extends BasePostRepository<Post>, JpaSpecificationExecutor<Post> {

    @Query("select sum(p.visits) from Post p")
    Long countVisit();

    @Query("select sum(p.likes) from Post p")
    Long countLike();
}
