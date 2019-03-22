package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.model.enums.PostStatus;
import cc.ryanc.halo.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;

import java.util.Optional;


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
     * @param pageable pageable
     * @return Page<Post>
     */
    @NonNull
    Page<Post> findAllByStatus(@NonNull PostStatus status, @NonNull Pageable pageable);

    /**
     * Counts posts by status and type.
     *
     * @param status status
     * @return posts count
     */
    long countByStatus(@NonNull PostStatus status);

    /**
     * Count by post url.
     *
     * @param url post url must not be blank
     * @return the count
     */
    long countByUrl(@NonNull String url);

    /**
     * Count by not url and post id not in.
     *
     * @param id  post id must not be null
     * @param url post url must not be null
     * @return the count
     */
    long countByIdNotAndUrl(@NonNull Integer id, @NonNull String url);

    /**
     * Get post by url
     *
     * @param url post url
     * @return Optional<Post>
     */
    Optional<Post> getByUrl(@NonNull String url);
}
