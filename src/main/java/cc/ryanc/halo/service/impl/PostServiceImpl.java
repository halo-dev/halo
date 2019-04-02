package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.exception.AlreadyExistsException;
import cc.ryanc.halo.exception.NotFoundException;
import cc.ryanc.halo.model.dto.CategoryOutputDTO;
import cc.ryanc.halo.model.dto.TagOutputDTO;
import cc.ryanc.halo.model.dto.post.PostMinimalOutputDTO;
import cc.ryanc.halo.model.dto.post.PostSimpleOutputDTO;
import cc.ryanc.halo.model.entity.*;
import cc.ryanc.halo.model.enums.PostStatus;
import cc.ryanc.halo.model.vo.ArchiveMonthVO;
import cc.ryanc.halo.model.vo.ArchiveYearVO;
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
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;

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

    private final CommentService commentService;

    public PostServiceImpl(PostRepository postRepository,
                           TagService tagService,
                           CategoryService categoryService,
                           PostTagService postTagService,
                           PostCategoryService postCategoryService,
                           CommentService commentService) {
        super(postRepository);
        this.postRepository = postRepository;
        this.tagService = tagService;
        this.categoryService = categoryService;
        this.postTagService = postTagService;
        this.postCategoryService = postCategoryService;
        this.commentService = commentService;
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

        PageRequest latestPageable = PageRequest.of(0, top, Sort.by(DESC, "editTime"));

        return listAll(latestPageable);
    }

    @Override
    public Page<Post> pageBy(PostStatus status, Pageable pageable) {
        Assert.notNull(status, "Post status must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        return postRepository.findAllByStatus(status, pageable);
    }

    /**
     * List by status and type
     *
     * @param status   status
     * @param pageable pageable
     * @return Page<PostSimpleOutputDTO>
     */
    @Override
    public Page<PostSimpleOutputDTO> pageSimpleDtoByStatus(PostStatus status, Pageable pageable) {
        return pageBy(status, pageable).map(post -> new PostSimpleOutputDTO().convertFrom(post));
    }

    @Override
    public Page<PostListVO> pageListVoBy(PostStatus status, Pageable pageable) {
        Page<Post> postPage = pageBy(status, pageable);

        List<Post> posts = postPage.getContent();

        Set<Integer> postIds = ServiceUtils.fetchProperty(posts, Post::getId);

        // Get tag list map
        Map<Integer, List<Tag>> tagListMap = postTagService.listTagListMapBy(postIds);

        // Get category list map
        Map<Integer, List<Category>> categoryListMap = postCategoryService.listCategoryListMap(postIds);

        // Get comment count
        Map<Integer, Long> commentCountMap = commentService.countByPostIds(postIds);


        return postPage.map(post -> {
            PostListVO postListVO = new PostListVO().convertFrom(post);

            Optional.ofNullable(tagListMap.get(post.getId())).orElseGet(LinkedList::new);

            // Set tags
            postListVO.setTags(Optional.ofNullable(tagListMap.get(post.getId()))
                    .orElseGet(LinkedList::new)
                    .stream()
                    .map(tag -> new TagOutputDTO().<TagOutputDTO>convertFrom(tag))
                    .collect(Collectors.toList()));

            // Set categories
            postListVO.setCategories(Optional.ofNullable(categoryListMap.get(post.getId()))
                    .orElseGet(LinkedList::new)
                    .stream()
                    .map(category -> new CategoryOutputDTO().<CategoryOutputDTO>convertFrom(category))
                    .collect(Collectors.toList()));

            // Set comment count
            postListVO.setCommentCount(commentCountMap.getOrDefault(post.getId(), 0L));

            return postListVO;
        });
    }

    /**
     * Counts posts by status and type
     *
     * @param status status
     * @return posts count
     */
    @Override
    public Long countByStatus(PostStatus status) {
        return postRepository.countByStatus(status);
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
            // For updating
            count = postRepository.countByIdNotAndUrl(post.getId(), post.getUrl());
        } else {
            // For creating
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
     * Gets post by url.
     *
     * @param url post url.
     * @return Post
     */
    @Override
    public Post getByUrl(String url) {
        return postRepository.getByUrl(url).orElseThrow(() -> new NotFoundException("The post does not exist").setErrorData(url));
    }

    @Override
    public PostDetailVO getDetailVoBy(Integer postId) {
        Assert.notNull(postId, "post id must not be null");

        Post post = getById(postId);

        // Convert to post detail vo
        return convertTo(post);
    }

    @Override
    public long countVisit() {
        return Optional.ofNullable(postRepository.countVisit()).orElse(0L);
    }

    @Override
    public long countLike() {
        return Optional.ofNullable(postRepository.countLike()).orElse(0L);
    }

    @Override
    public List<ArchiveYearVO> listYearArchives() {
        // Get all posts
        List<Post> posts = postRepository.findAllByStatus(PostStatus.PUBLISHED, Sort.by(DESC, "createTime"));

        Map<Integer, List<Post>> yearPostMap = new HashMap<>(8);

        posts.forEach(post -> {
            Calendar calendar = DateUtils.convertTo(post.getCreateTime());
            yearPostMap.computeIfAbsent(calendar.get(Calendar.YEAR), year -> new LinkedList<>())
                    .add(post);
        });


        List<ArchiveYearVO> archives = new LinkedList<>();

        yearPostMap.forEach((year, postList) -> {
            // Build archive
            ArchiveYearVO archive = new ArchiveYearVO();
            archive.setYear(year);
            archive.setPosts(convertTo(postList));

            // Add archive
            archives.add(archive);
        });

        // Sort this list
        archives.sort(new ArchiveYearVO.ArchiveComparator());

        return archives;
    }

    @Override
    public List<ArchiveMonthVO> listMonthArchives() {
        // Get all posts
        List<Post> posts = postRepository.findAllByStatus(PostStatus.PUBLISHED, Sort.by(DESC, "createTime"));

        Map<Integer, Map<Integer, List<Post>>> yearMonthPostMap = new HashMap<>(8);

        posts.forEach(post -> {
            Calendar calendar = DateUtils.convertTo(post.getCreateTime());

            yearMonthPostMap.computeIfAbsent(calendar.get(Calendar.YEAR), year -> new HashMap<>())
                    .computeIfAbsent((calendar.get(Calendar.MONTH) + 1), month -> new LinkedList<>())
                    .add(post);
        });

        List<ArchiveMonthVO> archives = new LinkedList<>();

        yearMonthPostMap.forEach((year, monthPostMap) -> monthPostMap.forEach((month, postList) -> {
            ArchiveMonthVO archive = new ArchiveMonthVO();
            archive.setYear(year);
            archive.setMonth(month);
            archive.setPosts(convertTo(postList));

            archives.add(archive);
        }));

        // Sort this list
        archives.sort(new ArchiveMonthVO.ArchiveComparator());

        return archives;
    }

    @Override
    @Transactional
    public Post removeById(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        log.debug("Removing post: [{}]", postId);

        // Remove post tags
        List<PostTag> postTags = postTagService.removeByPostId(postId);

        log.debug("Removed post tags: [{}]", postTags);

        // Remove post categories
        List<PostCategory> postCategories = postCategoryService.removeByPostId(postId);

        log.debug("Removed post categories: [{}]", postCategories);

        return super.removeById(postId);
    }

    /**
     * Converts to post minimal output dto.
     *
     * @param posts a list of post
     * @return a list of post minimal output dto
     */
    @NonNull
    private List<PostMinimalOutputDTO> convertTo(@NonNull List<Post> posts) {
        if (CollectionUtils.isEmpty(posts)) {
            return Collections.emptyList();
        }

        // Convert
        return posts.stream().map(post -> new PostMinimalOutputDTO().<PostMinimalOutputDTO>convertFrom(post))
                .collect(Collectors.toList());
    }

    /**
     * Converts to post detail vo.
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
     * Converts to post detail vo.
     *
     * @param post                  post must not be null
     * @param tagIdSetSupplier      tag id set supplier
     * @param categoryIdSetSupplier category id set supplier
     * @return post detail vo
     */
    @NonNull
    private PostDetailVO convertTo(@NonNull Post post, @Nullable Supplier<Set<Integer>> tagIdSetSupplier, @Nullable Supplier<Set<Integer>> categoryIdSetSupplier) {
        Assert.notNull(post, "Post must not be null");

        PostDetailVO postDetailVO = new PostDetailVO().convertFrom(post);

        // Get post tag ids
        postDetailVO.setTagIds(tagIdSetSupplier == null ? Collections.emptySet() : tagIdSetSupplier.get());

        // Get post category ids
        postDetailVO.setCategoryIds(categoryIdSetSupplier == null ? Collections.emptySet() : categoryIdSetSupplier.get());

        return postDetailVO;
    }
}
