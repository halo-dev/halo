package run.halo.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import run.halo.app.model.dto.post.PostMinimalDTO;
import run.halo.app.model.dto.post.PostSimpleDTO;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.params.PostQuery;
import run.halo.app.model.vo.ArchiveMonthVO;
import run.halo.app.model.vo.ArchiveYearVO;
import run.halo.app.model.vo.PostDetailVO;
import run.halo.app.model.vo.PostListVO;
import run.halo.app.service.base.CrudService;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Post service.
 *
 * @author johnniang
 * @author RYAN0UP
 */
public interface PostService extends CrudService<Post, Integer> {

    /**
     * Lists latest posts of minimal.
     *
     * @param top top number must not be less than 0
     * @return latest posts of minimal
     */
    @NonNull
    Page<PostMinimalDTO> pageLatestOfMinimal(int top);


    /**
     * Lists latest posts of simple .
     *
     * @param top top number must not be less than 0
     * @return latest posts of simple
     */
    @NonNull
    Page<PostSimpleDTO> pageLatestOfSimple(int top);


    /**
     * Lists latest posts.
     *
     * @param top top number must not be less than 0
     * @return latest posts
     */
    @NonNull
    Page<Post> pageLatest(int top);

    /**
     * Lists by status.
     *
     * @param status   post status must not be null
     * @param pageable page info must not be null
     * @return a page of post
     */
    @NonNull
    Page<Post> pageBy(@NonNull PostStatus status, @NonNull Pageable pageable);

    /**
     * Pages posts.
     *
     * @param postQuery post query must not be null
     * @param pageable  page info must not be null
     * @return a page of post
     */
    @NonNull
    Page<Post> pageBy(@NonNull PostQuery postQuery, @NonNull Pageable pageable);

    /**
     * Pages post by keyword
     *
     * @param keyword  keyword
     * @param pageable pageable
     * @return a page of post
     */
    @NonNull
    Page<Post> pageBy(@NonNull String keyword, @NonNull Pageable pageable);

    /**
     * Lists simple output dto by status.
     *
     * @param status   post status must not be null
     * @param pageable page info must not be null
     * @return Page<PostSimpleDTO>
     */
    @NonNull
    Page<PostSimpleDTO> pageSimpleDtoByStatus(@NonNull PostStatus status, @NonNull Pageable pageable);

    /**
     * Lists page list vo by status and pageable.
     *
     * @param status   post status must not be null
     * @param pageable page info must not be null
     * @return a page of page list vo
     */
    @NonNull
    Page<PostListVO> pageListVoBy(@NonNull PostStatus status, @NonNull Pageable pageable);

    /**
     * Count posts by status.
     *
     * @param status status
     * @return posts count
     */
    Long countByStatus(PostStatus status);

    /**
     * Creates post by post param.
     *
     * @param post        post must not be null
     * @param tagIds      tag id set
     * @param categoryIds category id set
     * @return post created
     */
    @NonNull
    @Transactional
    PostDetailVO createBy(@NonNull Post post, Set<Integer> tagIds, Set<Integer> categoryIds);

    /**
     * Updates post by post, tag id set and category id set.
     *
     * @param postToUpdate post to update must not be null
     * @param tagIds       tag id set
     * @param categoryIds  category id set
     * @return updated post
     */
    @NonNull
    @Transactional
    PostDetailVO updateBy(@NonNull Post postToUpdate, Set<Integer> tagIds, Set<Integer> categoryIds);

    /**
     * Get post by url.
     *
     * @param url post url.
     * @return Post
     */
    @NonNull
    Post getByUrl(@NonNull String url);

    /**
     * Gets post by post status and url.
     *
     * @param status post status must not be null
     * @param url    post url must not be blank
     * @return post info
     */
    @NonNull
    Post getBy(@NonNull PostStatus status, @NonNull String url);

    /**
     * Get post detail vo by post id.
     *
     * @param postId post id must not be null
     * @return post detail vo
     */
    @NonNull
    PostDetailVO getDetailVoBy(@NonNull Integer postId);

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
     * Increase post likes(1).
     *
     * @param postId post id must not be null
     */
    @Transactional
    void increaseLike(@NonNull Integer postId);

    /**
     * Lists year archives.
     *
     * @return a list of year archive
     */
    @NonNull
    List<ArchiveYearVO> listYearArchives();

    /**
     * Lists month archives.
     *
     * @return a list of month archive
     */
    @NonNull
    List<ArchiveMonthVO> listMonthArchives();

    /**
     * Converts to a page of post simple output dto.
     *
     * @param postPage post page must not be null
     * @return a page of post simple output dto
     */
    @NonNull
    Page<PostSimpleDTO> convertToSimpleDto(@NonNull Page<Post> postPage);

    /**
     * Converts to a page of post list vo.
     *
     * @param postPage post page must not be null
     * @return a page of post list vo
     */
    @NonNull
    Page<PostListVO> convertToListVo(@NonNull Page<Post> postPage);

    /**
     * Lists all posts by post status.
     *
     * @param status post status must not be null
     * @return a list of post
     */
    @NonNull
    List<Post> listAllBy(@NonNull PostStatus status);

    /**
     * Filters post content if the password is not blank.
     *
     * @param post original post must not be null
     * @return filtered post
     */
    @NonNull
    Post filterIfEncrypt(@NonNull Post post);

    /**
     * Gets previous post.
     *
     * @param date date must not be null
     * @return an optional post
     */
    @NonNull
    Optional<Post> getPrePost(@NonNull Date date);

    /**
     * Gets next post.
     *
     * @param date date must not be null
     * @return an optional post
     */
    @NonNull
    Optional<Post> getNextPost(@NonNull Date date);

    /**
     * Lists previous posts.
     *
     * @param date date must not be null
     * @param size previous max post size
     * @return a list of previous post
     */
    @NonNull
    List<Post> listPrePosts(@NonNull Date date, int size);

    /**
     * Lits next posts.
     *
     * @param date date must not be null
     * @param size next max post size
     * @return a list of next post
     */
    @NonNull
    List<Post> listNextPosts(@NonNull Date date, int size);
}
