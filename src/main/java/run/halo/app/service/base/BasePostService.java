package run.halo.app.service.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import run.halo.app.model.dto.post.BasePostDetailDTO;
import run.halo.app.model.dto.post.BasePostMinimalDTO;
import run.halo.app.model.dto.post.BasePostSimpleDTO;
import run.halo.app.model.entity.BasePost;
import run.halo.app.model.enums.PostStatus;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Base post service implementation.
 *
 * @author johnniang
 * @date 19-4-24
 */
public interface BasePostService<POST extends BasePost> extends CrudService<POST, Integer> {

    /**
     * Counts visit total number.
     *
     * @return visit total number
     */
    long countVisit();

    /**
     * Counts like total number.
     *
     * @return like total number
     */
    long countLike();

    /**
     * Count posts by status.
     *
     * @param status status
     * @return posts count
     */
    long countByStatus(PostStatus status);

    /**
     * Get post by url.
     *
     * @param url post url.
     * @return Post
     */
    @NonNull
    POST getByUrl(@NonNull String url);

    /**
     * Gets post by post status and url.
     *
     * @param status post status must not be null
     * @param url    post url must not be blank
     * @return post info
     */
    @NonNull
    POST getBy(@NonNull PostStatus status, @NonNull String url);

    /**
     * Lists all posts by post status.
     *
     * @param status post status must not be null
     * @return a list of post
     */
    @NonNull
    List<POST> listAllBy(@NonNull PostStatus status);

    /**
     * Lists previous posts.
     *
     * @param date date must not be null
     * @param size previous max post size
     * @return a list of previous post
     */
    @NonNull
    List<POST> listPrePosts(@NonNull Date date, int size);

    /**
     * Lits next posts.
     *
     * @param date date must not be null
     * @param size next max post size
     * @return a list of next post
     */
    @NonNull
    List<POST> listNextPosts(@NonNull Date date, int size);

    /**
     * Gets previous post.
     *
     * @param date date must not be null
     * @return an optional post
     */
    @NonNull
    Optional<POST> getPrePost(@NonNull Date date);

    /**
     * Gets next post.
     *
     * @param date date must not be null
     * @return an optional post
     */
    @NonNull
    Optional<POST> getNextPost(@NonNull Date date);

    /**
     * Pages latest posts.
     *
     * @param top top number must not be less than 0
     * @return latest posts
     */
    @NonNull
    Page<POST> pageLatest(int top);

    /**
     * Lists latest posts.
     *
     * @param top top number must not be less than 0
     * @return latest posts
     */
    @NonNull
    List<POST> listLatest(int top);

    /**
     * Gets a page of sheet.
     *
     * @param pageable page info must not be null
     * @return a page of sheet
     */
    @NonNull
    Page<POST> pageBy(@NonNull Pageable pageable);

    /**
     * Lists by status.
     *
     * @param status   post status must not be null
     * @param pageable page info must not be null
     * @return a page of post
     */
    @NonNull
    Page<POST> pageBy(@NonNull PostStatus status, @NonNull Pageable pageable);

    /**
     * Increases post visits.
     *
     * @param visits visits must not be less than 1
     * @param postId post id must not be null
     */
    @Transactional
    void increaseVisit(long visits, @NonNull Integer postId);

    /**
     * Increase post likes.
     *
     * @param likes  likes must not be less than 1
     * @param postId post id must not be null
     */
    @Transactional
    void increaseLike(long likes, @NonNull Integer postId);

    /**
     * Increases post visits (1).
     *
     * @param postId post id must not be null
     */
    @Transactional
    void increaseVisit(@NonNull Integer postId);

    /**
     * Increases post likes(1).
     *
     * @param postId post id must not be null
     */
    @Transactional
    void increaseLike(@NonNull Integer postId);

    /**
     * Creates or updates by post.
     *
     * @param post post must not be null
     * @return created or updated post
     */
    @NonNull
    POST createOrUpdateBy(@NonNull POST post);

    /**
     * Filters post content if the password is not blank.
     *
     * @param post original post must not be null
     * @return filtered post
     */
    @NonNull
    POST filterIfEncrypt(@NonNull POST post);

    @NonNull
    BasePostMinimalDTO convertToMinimal(@NonNull POST post);

    @NonNull
    List<BasePostMinimalDTO> convertToMinimal(@Nullable List<POST> posts);

    @NonNull
    Page<BasePostMinimalDTO> convertToMinimal(@NonNull Page<POST> postPage);

    @NonNull
    BasePostSimpleDTO convertToSimple(@NonNull POST post);

    @NonNull
    List<BasePostSimpleDTO> convertToSimple(@Nullable List<POST> posts);

    @NonNull
    Page<BasePostSimpleDTO> convertToSimple(@NonNull Page<POST> postPage);

    @NonNull
    BasePostDetailDTO convertToDetail(@NonNull POST post);
}
