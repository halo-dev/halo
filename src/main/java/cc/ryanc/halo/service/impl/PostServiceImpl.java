package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.exception.AlreadyExistsException;
import cc.ryanc.halo.exception.NotFoundException;
import cc.ryanc.halo.model.dto.CategoryOutputDTO;
import cc.ryanc.halo.model.dto.TagOutputDTO;
import cc.ryanc.halo.model.dto.post.PostMinimalOutputDTO;
import cc.ryanc.halo.model.dto.post.PostSimpleOutputDTO;
import cc.ryanc.halo.model.entity.*;
import cc.ryanc.halo.model.enums.PostStatus;
import cc.ryanc.halo.model.enums.PostType;
import cc.ryanc.halo.model.vo.PostDetailVO;
import cc.ryanc.halo.model.vo.PostListVO;
import cc.ryanc.halo.repository.PostRepository;
import cc.ryanc.halo.service.*;
import cc.ryanc.halo.service.base.AbstractCrudService;
import cc.ryanc.halo.utils.DateUtils;
import cc.ryanc.halo.utils.MarkdownUtils;
import cc.ryanc.halo.utils.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Post service implementation.
 *
 * @author johnniang
 * @author RYAN0UP
 */
@Slf4j
@Service
public class PostServiceImpl extends AbstractCrudService<Post, Integer> implements PostService {

    private final PostRepository postRepository;

    private final TagService tagService;

    private final CategoryService categoryService;

    private final PostTagService postTagService;

    private final PostCategoryService postCategoryService;

    public PostServiceImpl(PostRepository postRepository,
                           TagService tagService,
                           CategoryService categoryService,
                           PostTagService postTagService,
                           PostCategoryService postCategoryService) {
        super(postRepository);
        this.postRepository = postRepository;
        this.tagService = tagService;
        this.categoryService = categoryService;
        this.postTagService = postTagService;
        this.postCategoryService = postCategoryService;
    }

    /**
     * Save post with tags and categories
     *
     * @param post       post
     * @param tags       tags
     * @param categories categories
     * @return saved post
     */
    @Override
    public Post save(Post post, List<Tag> tags, List<Category> categories) {
        // TODO 保存文章以及对应标签和分类
        return null;
    }

    /**
     * Remove post and relationship
     *
     * @param id id
     */
    @Override
    public void remove(Integer id) {
        // TODO 删除文章以及关联关系
    }

    @Override
    public Page<PostMinimalOutputDTO> pageLatestOfMinimal(int top) {
        return pageLatest(top).map(post -> new PostMinimalOutputDTO().convertFrom(post));
    }

    @Override
    public Page<PostSimpleOutputDTO> pageLatestOfSimple(int top) {
        return pageLatest(top).map(post -> new PostSimpleOutputDTO().convertFrom(post));
    }

    @Override
    public Page<Post> pageLatest(int top) {
        Assert.isTrue(top > 0, "Top number must not be less than 0");

        PageRequest latestPageable = PageRequest.of(0, top, Sort.by(Sort.Direction.DESC, "editTime"));

        return listAll(latestPageable);
    }

    @Override
    public Page<Post> pageBy(PostStatus status, PostType type, Pageable pageable) {
        Assert.notNull(status, "Post status must not be null");
        Assert.notNull(type, "Post type must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        return postRepository.findAllByStatusAndType(status, type, pageable);
    }

    /**
     * List by status and type
     *
     * @param status   status
     * @param type     type
     * @param pageable pageable
     * @return Page<PostSimpleOutputDTO>
     */
    @Override
    public Page<PostSimpleOutputDTO> pageSimpleDtoByStatus(PostStatus status, PostType type, Pageable pageable) {
        return pageBy(status, type, pageable).map(post -> new PostSimpleOutputDTO().convertFrom(post));
    }

    @Override
    public Page<PostListVO> pageListVoBy(PostStatus status, PostType type, Pageable pageable) {
        Page<Post> postPage = pageBy(status, type, pageable);

        List<Post> posts = postPage.getContent();

        Set<Integer> postIds = ServiceUtils.fetchProperty(posts, Post::getId);

        Map<Integer, List<Tag>> tagListMap = postTagService.listTagListMapBy(postIds);

        Map<Integer, List<Category>> categoryListMap = postCategoryService.listCategoryListMap(postIds);

        return postPage.map(post -> {
            PostListVO postListVO = new PostListVO().convertFrom(post);

            // Set tags
            List<TagOutputDTO> tagOutputDTOS = tagListMap.get(post.getId()).stream().map(tag -> (TagOutputDTO) new TagOutputDTO().convertFrom(tag)).collect(Collectors.toList());
            postListVO.setTags(tagOutputDTOS);

            // Set categories
            List<CategoryOutputDTO> categoryOutputDTOS = categoryListMap.get(post.getId()).stream().map(category -> (CategoryOutputDTO) new CategoryOutputDTO().convertFrom(category)).collect(Collectors.toList());
            postListVO.setCategories(categoryOutputDTOS);

            return postListVO;
        });
    }

    /**
     * Count posts by status and type
     *
     * @param status status
     * @param type   type
     * @return posts count
     */
    @Override
    public Long countByStatus(PostStatus status, PostType type) {
        return postRepository.countByStatusAndType(status, type);
    }

    @Override
    public PostDetailVO createBy(Post postToCreate, Set<Integer> tagIds, Set<Integer> categoryIds) {
        return createOrUpdate(postToCreate, tagIds, categoryIds, this::create);
    }

    @Override
    public PostDetailVO updateBy(Post postToUpdate, Set<Integer> tagIds, Set<Integer> categoryIds) {
        // Set edit time
        postToUpdate.setEditTime(DateUtils.now());

        return createOrUpdate(postToUpdate, tagIds, categoryIds, this::update);
    }

    private PostDetailVO createOrUpdate(@NonNull Post post, Set<Integer> tagIds, Set<Integer> categoryIds, @NonNull Function<Post, Post> postOperation) {
        Assert.notNull(post, "Post param must not be null");
        Assert.notNull(postOperation, "Post operation must not be null");

        // Check url
        long count;
        if (post.getId() != null) {
            count = postRepository.countByIdNotAndUrl(post.getId(), post.getUrl());
        } else {
            count = postRepository.countByUrl(post.getUrl());
        }

        if (count > 0) {
            throw new AlreadyExistsException("The post url has been exist already").setErrorData(post.getUrl());
        }

        // Render content
        post.setFormatContent(MarkdownUtils.renderMarkdown(post.getOriginalContent()));

        // Update post
        post = postOperation.apply(post);

        // List all tags
        List<Tag> tags = tagService.listAllByIds(tagIds);

        // List all categories
        List<Category> categories = categoryService.listAllByIds(categoryIds);

        // Create post tags
        List<PostTag> postTags = postTagService.mergeOrCreateByIfAbsent(post.getId(), ServiceUtils.fetchProperty(tags, Tag::getId));

        log.debug("Created post tags: [{}]", postTags);

        // Create post categories
        List<PostCategory> postCategories = postCategoryService.mergeOrCreateByIfAbsent(post.getId(), ServiceUtils.fetchProperty(categories, Category::getId));

        log.debug("Created post categories: [{}]", postCategories);

        // Convert to post detail vo
        return convertTo(post,
                () -> ServiceUtils.fetchProperty(postTags, PostTag::getTagId),
                () -> ServiceUtils.fetchProperty(postCategories, PostCategory::getCategoryId));
    }

    /**
     * Get post by url.
     *
     * @param url  post url.
     * @param type post type enum.
     * @return Post
     */
    @Override
    public Post getByUrl(String url, PostType type) {
        return postRepository.getByUrlAndType(url, type).orElseThrow(() -> new NotFoundException("The post does not exist").setErrorData(url));
    }

    @Override
    public PostDetailVO getDetailVoBy(Integer postId) {
        Assert.notNull(postId, "post id must not be null");

        Post post = getById(postId);

        // Convert to post detail vo
        return convertTo(post);
    }

    /**
     * Convert to post detail vo.
     *
     * @param post post must not be null
     * @return post detail vo
     */
    @NonNull
    private PostDetailVO convertTo(@NonNull Post post) {
        return convertTo(post,
                () -> postTagService.listTagIdsByPostId(post.getId()),
                () -> postCategoryService.listCategoryIdsByPostId(post.getId()));
    }

    /**
     * Convert to post detail vo.
     *
     * @param post                  post must not be null
     * @param tagIdSetSupplier      tag id set supplier
     * @param categoryIdSetSupplier category id set supplier
     * @return post detail vo
     */
    @NonNull
    private PostDetailVO convertTo(@NonNull Post post, Supplier<Set<Integer>> tagIdSetSupplier, Supplier<Set<Integer>> categoryIdSetSupplier) {
        Assert.notNull(post, "Post must not be null");

        PostDetailVO postDetailVO = new PostDetailVO().convertFrom(post);

        // Get post tag ids
        postDetailVO.setTagIds(tagIdSetSupplier == null ? Collections.emptySet() : tagIdSetSupplier.get());

        // Get post category ids
        postDetailVO.setCategoryIds(categoryIdSetSupplier == null ? Collections.emptySet() : categoryIdSetSupplier.get());

        return postDetailVO;
    }
}
