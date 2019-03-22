package cc.ryanc.halo.repository.base;

import cc.ryanc.halo.model.entity.BasePost;
import cc.ryanc.halo.model.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * Base post repository.
 *
 * @author johnniang
 * @date 3/22/19
 */
@NoRepositoryBean
public interface BasePostRepository<DOMAIN extends BasePost> extends BaseRepository<DOMAIN, Integer> {

    /**
     * Finds posts by status and type.
     *
     * @param status   status
     * @param pageable pageable
     * @return Page<Post>
     */
    @NonNull
    Page<DOMAIN> findAllByStatus(@NonNull PostStatus status, @NonNull Pageable pageable);

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
    Optional<DOMAIN> getByUrl(@NonNull String url);

}
