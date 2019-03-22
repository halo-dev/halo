package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.repository.base.BasePostRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


/**
 * Post repository.
 *
 * @author johnniang
 * @author RYAN0UP
 */
public interface PostRepository extends BasePostRepository<Post>, JpaSpecificationExecutor<Post> {

}
