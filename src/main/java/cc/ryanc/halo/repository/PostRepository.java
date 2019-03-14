package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


/**
 * Post repository.
 *
 * @author johnniang
 * @author RYAN0UP
 */
public interface PostRepository extends BaseRepository<Post, Integer>, JpaSpecificationExecutor<Post> {

    /**
     * Find posts by status and type
     *
     * @param status   status
     * @param type     type
     * @param pageable pageable
     *
     * @return Page<Post>
     */
    Page<Post> queryAllByStatusAndType(int status, Integer type, Pageable pageable);

    /**
     * Count posts by status and type
     *
     * @param status status
     * @param type   type
     *
     * @return posts count
     */
    Long countAllByStatusAndType(int status, Integer type);
}
