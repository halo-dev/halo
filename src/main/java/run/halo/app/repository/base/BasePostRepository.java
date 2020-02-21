package run.halo.app.repository.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import run.halo.app.model.entity.BasePost;
import run.halo.app.model.enums.PostStatus;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Base post repository.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-22
 */
public interface BasePostRepository<POST extends BasePost> extends BaseRepository<POST, Integer> {

    /**
     * Counts visits. (Need to be overridden)
     *
     * @return total visits
     */
    @Query("select sum(p.visits) from BasePost p")
    Long countVisit();

    /**
     * Counts likes. (Need to be overridden)
     *
     * @return total likes
     */
    @Query("select sum(p.likes) from BasePost p")
    Long countLike();

    /**
     * Finds posts by status and pageable.
     *
     * @param status   post status must not be null
     * @param pageable page info must not be null
     * @return a page of post
     */
    @NonNull
    Page<POST> findAllByStatus(@NonNull PostStatus status, @NonNull Pageable pageable);

    /**
     * Finds posts by status.
     *
     * @param status post staus must not be null
     * @return a list of post
     */
    @NonNull
    List<POST> findAllByStatus(@NonNull PostStatus status);

    /**
     * Finds posts by status.
     *
     * @param status post staus must not be null
     * @param sort   sort info must not be null
     * @return a list of post
     */
    @NonNull
    List<POST> findAllByStatus(@NonNull PostStatus status, @NonNull Sort sort);

    /**
     * Finds all post by status and create time before.
     *
     * @param status     status must not be null
     * @param createTime create time must not be null
     * @param pageable   page info must not be null
     * @return a page of post
     */
    @NonNull
    Page<POST> findAllByStatusAndCreateTimeBefore(@NonNull PostStatus status, @NonNull Date createTime, @NonNull Pageable pageable);

    /**
     * Finds all post by status and create time after.
     *
     * @param status     status must not be null
     * @param createTime create time must not be null
     * @param pageable   page info must not be null
     * @return a page of post
     */
    @NonNull
    Page<POST> findAllByStatusAndCreateTimeAfter(@NonNull PostStatus status, @NonNull Date createTime, @NonNull Pageable pageable);

    /**
     * Gets post by url and status.
     *
     * @param url    url must not be blank
     * @param status status must not be null
     * @return an optional post
     */
    @NonNull
    Optional<POST> getByUrlAndStatus(@NonNull String url, @NonNull PostStatus status);

    /**
     * Gets post by id and status.
     *
     * @param id     id must not be blank
     * @param status status must not be null
     * @return an optional post
     */
    @NonNull
    Optional<POST> getByIdAndStatus(@NonNull Integer id, @NonNull PostStatus status);


    /**
     * Counts posts by status and type.
     *
     * @param status status
     * @return posts count
     */
    long countByStatus(@NonNull PostStatus status);

    /**
     * Determine if the url exists.
     *
     * @param url url must not be null.
     * @return true or false.
     */
    boolean existsByUrl(@NonNull String url);

    /**
     * Determine if the url exists.
     *
     * @param id  post id must not be null.
     * @param url url must not be null.
     * @return true or false.
     */
    boolean existsByIdNotAndUrl(@NonNull Integer id, @NonNull String url);

    /**
     * Get post by url
     *
     * @param url post url
     * @return Optional<Post>
     */
    Optional<POST> getByUrl(@NonNull String url);

    /**
     * Updates post visits.
     *
     * @param visits visit delta
     * @param postId post id must not be null
     * @return updated rows
     */
    @Modifying
    @Query("update BasePost p set p.visits = p.visits + :visits where p.id = :postId")
    int updateVisit(@Param("visits") long visits, @Param("postId") @NonNull Integer postId);

    /**
     * Updates post likes.
     *
     * @param likes  likes delta
     * @param postId post id must not be null
     * @return updated rows
     */
    @Modifying
    @Query("update BasePost p set p.likes = p.likes + :likes where p.id = :postId")
    int updateLikes(@Param("likes") long likes, @Param("postId") @NonNull Integer postId);

    /**
     * Updates post original content.
     *
     * @param content content could be blank but disallow to be null
     * @param postId  post id must not be null
     * @return updated rows
     */
    @Modifying
    @Query("update BasePost p set p.originalContent = :content where p.id = :postId")
    int updateOriginalContent(@Param("content") @NonNull String content, @Param("postId") @NonNull Integer postId);

    /**
     * Updates post status by post id.
     *
     * @param status post status must not be null.
     * @param postId post id must not be null.
     * @return updated rows.
     */
    @Modifying
    @Query("update BasePost p set p.status = :status where p.id = :postId")
    int updateStatus(@Param("status") @NonNull PostStatus status, @Param("postId") @NonNull Integer postId);

    /**
     * Updates post format content by post id.
     *
     * @param formatContent format content must not be null.
     * @param postId        post id must not be null.
     * @return updated rows.
     */
    @Modifying
    @Query("update BasePost p set p.formatContent = :formatContent where p.id = :postId")
    int updateFormatContent(@Param("formatContent") @NonNull String formatContent, @Param("postId") @NonNull Integer postId);
}
