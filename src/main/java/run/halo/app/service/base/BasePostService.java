package run.halo.app.service.base;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.model.entity.BasePost;
import run.halo.app.model.entity.Content;
import run.halo.app.model.entity.Content.PatchedContent;
import run.halo.app.model.enums.PostStatus;

/**
 * Base post service implementation.
 *
 * @author johnniang
 * @author ryanwang
 * @author guqing
 * @date 2019-04-24
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
     * Get post by slug.
     *
     * @param slug post slug.
     * @return Post
     */
    @NonNull
    POST getBySlug(@NonNull String slug);

    /**
     * Get post with the latest content by id.
     * content from patch log.
     *
     * @param postId post id.
     * @return post with the latest content.
     */
    POST getWithLatestContentById(Integer postId);

    /**
     * Gets post by post status and slug.
     *
     * @param status post status must not be null
     * @param slug post slug must not be blank
     * @return post info
     */
    @NonNull
    POST getBy(@NonNull PostStatus status, @NonNull String slug);

    /**
     * Gets post by post status and id.
     *
     * @param status post status must not be null
     * @param id post id must not be blank
     * @return post info
     */
    @NonNull
    POST getBy(@NonNull PostStatus status, @NonNull Integer id);

    /**
     * Gets content by post id.
     *
     * @param id post id.
     * @return a content of post.
     */
    Content getContentById(Integer id);

    /**
     * Gets the latest content by id.
     * content from patch log.
     *
     * @param id post id.
     * @return a latest content from patchLog of post.
     */
    PatchedContent getLatestContentById(Integer id);

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
     * @param post post must not be null
     * @param size previous max post size
     * @return a list of previous post
     */
    @NonNull
    List<POST> listPrevPosts(@NonNull POST post, int size);

    /**
     * Lits next posts.
     *
     * @param post post must not be null
     * @param size next max post size
     * @return a list of next post
     */
    @NonNull
    List<POST> listNextPosts(@NonNull POST post, int size);

    /**
     * Gets previous post.
     *
     * @param post post must not be null
     * @return an optional post
     */
    @NonNull
    Optional<POST> getPrevPost(@NonNull POST post);

    /**
     * Gets next post.
     *
     * @param post post must not be null
     * @return an optional post
     */
    @NonNull
    Optional<POST> getNextPost(@NonNull POST post);

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
     * @param status post status must not be null
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
    void increaseVisit(long visits, @NonNull Integer postId);

    /**
     * Increases post visits (1).
     *
     * @param postId post id must not be null
     */
    void increaseVisit(@NonNull Integer postId);

    /**
     * Increase post likes.
     *
     * @param likes likes must not be less than 1
     * @param postId post id must not be null
     */
    void increaseLike(long likes, @NonNull Integer postId);

    /**
     * Increases post likes(1).
     *
     * @param postId post id must not be null
     */
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
     * Updates draft content.
     *
     * @param content draft content could be blank
     * @param postId post id must not be null
     * @return updated post
     */
    @NonNull
    POST updateDraftContent(@Nullable String content, String originalContent,
        @NonNull Integer postId);

    /**
     * Updates post status.
     *
     * @param status post status must not be null
     * @param postId post id must not be null
     * @return updated post
     */
    @NonNull
    POST updateStatus(@NonNull PostStatus status, @NonNull Integer postId);

    /**
     * Updates post status by ids.
     *
     * @param ids post ids must not be null
     * @param status post status must not be null
     * @return updated posts
     */
    @NonNull
    List<POST> updateStatusByIds(@NonNull List<Integer> ids, @NonNull PostStatus status);

    /**
     * Generate description.
     *
     * @param content html content.
     * @return description
     */
    String generateDescription(@Nullable String content);
}
