package cc.ryanc.halo.service;

import cc.ryanc.halo.model.dto.post.PostMinimalOutputDTO;
import cc.ryanc.halo.model.dto.post.PostSimpleOutputDTO;
import cc.ryanc.halo.model.entity.Category;
import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.model.entity.Tag;
import cc.ryanc.halo.model.enums.PostStatus;
import cc.ryanc.halo.model.enums.PostType;
import cc.ryanc.halo.model.vo.PostListVO;
import cc.ryanc.halo.service.base.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Post service.
 *
 * @author johnniang
 * @author RYAN0UP
 */
public interface PostService extends CrudService<Post, Integer> {

    /**
     * Save post with tags and categories
     *
     * @param post       post
     * @param tags       tags
     * @param categories categories
     * @return saved post
     */
    Post save(Post post, List<Tag> tags, List<Category> categories);

    /**
     * Remove post and relationship
     *
     * @param id id
     */
    void remove(Integer id);

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
     * @param type     post type must not be null
     * @param pageable page info must not be null
     * @return Page<PostSimpleOutputDTO>
     */
    @NonNull
    Page<Post> pageBy(@NonNull PostStatus status, @NonNull PostType type, @NonNull Pageable pageable);


    /**
     * List simple output dto by status and type
     *
     * @param status   post status must not be null
     * @param type     post type must not be null
     * @param pageable page info must not be null
     * @return Page<PostSimpleOutputDTO>
     */
    @NonNull
    Page<PostSimpleOutputDTO> pageSimpleDtoByStatus(@NonNull PostStatus status, @NonNull PostType type, @NonNull Pageable pageable);

    /**
     * Lists page list vo by status, type and pageable.
     *
     * @param status   post status must not be null
     * @param type     post type must not be null
     * @param pageable page info must not be null
     * @return a page of page list vo
     */
    @NonNull
    Page<PostListVO> pageListVoBy(@NonNull PostStatus status, @NonNull PostType type, @NonNull Pageable pageable);

    /**
     * Count posts by status and type
     *
     * @param status status
     * @param type   type
     * @return posts count
     */
    Long countByStatus(PostStatus status, PostType type);

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
    Post createBy(@NonNull Post post, Set<Integer> tagIds, Set<Integer> categoryIds);

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
    Post updateBy(@NonNull Post postToUpdate, Set<Integer> tagIds, Set<Integer> categoryIds);

    /**
     * Get post by url.
     *
     * @param url  post url.
     * @param type post type enum.
     * @return Post
     */
    Post getByUrl(@NonNull String url, @NonNull PostType type);
}
