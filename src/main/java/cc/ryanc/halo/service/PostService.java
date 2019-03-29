package cc.ryanc.halo.service;

import cc.ryanc.halo.model.dto.post.PostMinimalOutputDTO;
import cc.ryanc.halo.model.dto.post.PostSimpleOutputDTO;
import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.model.enums.PostStatus;
import cc.ryanc.halo.model.vo.PostDetailVO;
import cc.ryanc.halo.model.vo.PostListVO;
import cc.ryanc.halo.service.base.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

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
    Page<PostMinimalOutputDTO> pageLatestOfMinimal(int top);


    /**
     * Lists latest posts of simple .
     *
     * @param top top number must not be less than 0
     * @return latest posts of simple
     */
    @NonNull
    Page<PostSimpleOutputDTO> pageLatestOfSimple(int top);


    /**
     * Lists latest posts.
     *
     * @param top top number must not be less than 0
     * @return latest posts
     */
    @NonNull
    Page<Post> pageLatest(int top);

    /**
     * List by status and type
     *
     * @param status   post status must not be null
     * @param pageable page info must not be null
     * @return Page<PostSimpleOutputDTO>
     */
    @NonNull
    Page<Post> pageBy(@NonNull PostStatus status, @NonNull Pageable pageable);


    /**
     * List simple output dto by status and type
     *
     * @param status   post status must not be null
     * @param pageable page info must not be null
     * @return Page<PostSimpleOutputDTO>
     */
    @NonNull
    Page<PostSimpleOutputDTO> pageSimpleDtoByStatus(@NonNull PostStatus status, @NonNull Pageable pageable);

    /**
     * Lists page list vo by status, type and pageable.
     *
     * @param status   post status must not be null
     * @param pageable page info must not be null
     * @return a page of page list vo
     */
    @NonNull
    Page<PostListVO> pageListVoBy(@NonNull PostStatus status, @NonNull Pageable pageable);

    /**
     * Count posts by status and type
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
    Post getByUrl(@NonNull String url);

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
}
