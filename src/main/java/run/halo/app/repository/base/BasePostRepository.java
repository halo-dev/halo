package run.halo.app.repository.base;

import run.halo.app.model.entity.BasePost;
import run.halo.app.model.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;

import java.util.List;
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
     * Finds posts by status and pageable.
     *
     * @param status   post status must not be null
     * @param pageable page info must not be null
     * @return a page of post
     */
    @NonNull
    Page<DOMAIN> findAllByStatus(@NonNull PostStatus status, @NonNull Pageable pageable);

    /**
     * Finds posts by status.
     *
     * @param status post staus must not be null
     * @return a list of post
     */
    @NonNull
    List<DOMAIN> findAllByStatus(@NonNull PostStatus status);

    /**
     * Finds posts by status.
     *
     * @param status post staus must not be null
     * @param sort   sort info must not be null
     * @return a list of post
     */
    @NonNull
    List<DOMAIN> findAllByStatus(@NonNull PostStatus status, @NonNull Sort sort);

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
