package cc.ryanc.halo.repository.v1;

import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


/**
 * Post repository.
 *
 * @author johnniang
 */
public interface PostRepository extends BaseRepository<Post, Integer>, JpaSpecificationExecutor<Post> {

}
