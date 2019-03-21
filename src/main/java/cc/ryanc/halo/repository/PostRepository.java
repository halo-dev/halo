package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.model.enums.PostStatus;
import cc.ryanc.halo.model.enums.PostType;
import cc.ryanc.halo.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;


/**
 * Post repository.
 *
 * @author johnniang
 * @author RYAN0UP
 */
public interface PostRepository extends BaseRepository<Post, Integer>, JpaSpecificationExecutor<Post> {

    /**
     * Finds posts by status and type.
     *
     * @param status   status
     * @param type     type
     * @param pageable pageable
     * @return Page<Post>
     */
    @NonNull
    Page<Post> findAllByStatusAndType(@NonNull PostStatus status, @NonNull PostType type, @NonNull Pageable pageable);

    /**
     * Counts posts by status and type.
     *
     * @param status status
     * @param type   type
     * @return posts count
     */
    long countByStatusAndType(@NonNull PostStatus status, @NonNull PostType type);

    /**
     * Count by post url.
     *
     * @param url post url must not be blank
     * @return the count
     */
    long countByUrl(@NonNull String url);
}
