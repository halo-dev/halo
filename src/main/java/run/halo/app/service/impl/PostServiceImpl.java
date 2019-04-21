package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.exception.AlreadyExistsException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.CategoryOutputDTO;
import run.halo.app.model.dto.TagOutputDTO;
import run.halo.app.model.dto.post.PostMinimalOutputDTO;
import run.halo.app.model.dto.post.PostSimpleOutputDTO;
import run.halo.app.model.entity.*;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.params.PostQuery;
import run.halo.app.model.vo.ArchiveMonthVO;
import run.halo.app.model.vo.ArchiveYearVO;
import run.halo.app.model.vo.PostDetailVO;
import run.halo.app.model.vo.PostListVO;
import run.halo.app.repository.PostRepository;
import run.halo.app.service.*;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.DateUtils;
import run.halo.app.utils.MarkdownUtils;
import run.halo.app.utils.ServiceUtils;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
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

    @Override
    public Page<Post> pageBy(PostQuery postQuery, Pageable pageable) {
        Assert.notNull(postQuery, "Post query must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        // Build specification and find all
        return postRepository.findAll(buildSpecByQuery(postQuery), pageable);
    }

    /**
     * Build specification by post query.
     *
     * @param postQuery post query must not be null
     * @return a post specification
     */
    @NonNull
    private Specification<Post> buildSpecByQuery(@NonNull PostQuery postQuery) {
        Assert.notNull(postQuery, "Post query must not be null");

        return (Specification<Post>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new LinkedList<>();

            if (postQuery.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), postQuery.getStatus()));
            }

            if (postQuery.getCategoryId() != null) {
                Subquery<Post> postSubquery = query.subquery(Post.class);
                Root<PostCategory> postCategoryRoot = postSubquery.from(PostCategory.class);
                postSubquery.select(postCategoryRoot.get("postId"));
                postSubquery.where(
                        criteriaBuilder.equal(root.get("id"), postCategoryRoot.get("postId")),
                        criteriaBuilder.equal(postCategoryRoot.get("categoryId"), postQuery.getCategoryId()));
                predicates.add(criteriaBuilder.exists(postSubquery));
            }

            if (postQuery.getKeyword() != null) {
                // Format like condition
                String likeCondition = String.format("%%%s%%", StringUtils.strip(postQuery.getKeyword()));

                // Build like predicate
                Predicate titleLike = criteriaBuilder.like(root.get("title"), likeCondition);
                Predicate originalContentLike = criteriaBuilder.like(root.get("originalContent"), likeCondition);

                predicates.add(criteriaBuilder.or(titleLike, originalContentLike));
            }

            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };
    }

    /**
     * List by status.
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

        return convertToListVo(postPage);
    }

    /**
     * Counts posts by status.
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

        yearMonthPostMap.forEach((year, monthPostMap) ->
                monthPostMap.forEach((month, postList) -> {
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

    @Override
    public Post getPrePostOfNullable(Date date) {
        return getPrePost(date).orElse(null);
    }

    @Override
    public Post getNextPostOfNullable(Date date) {
        return getNextPost(date).orElse(null);
    }

    @Override
    public Page<PostSimpleOutputDTO> convertToSimpleDto(@NonNull Page<Post> postPage) {
        Assert.notNull(postPage, "Post page must not be null");

        return postPage.map(post -> new PostSimpleOutputDTO().convertFrom(post));
    }

    @Override
    public Page<PostListVO> convertToListVo(Page<Post> postPage) {
        Assert.notNull(postPage, "Post page must not be null");

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
                    .filter(Objects::nonNull)
                    .map(category -> new CategoryOutputDTO().<CategoryOutputDTO>convertFrom(category))
                    .collect(Collectors.toList()));

            // Set comment count
            postListVO.setCommentCount(commentCountMap.getOrDefault(post.getId(), 0L));

            return postListVO;
        });
    }

    @Override
    public List<Post> listAllBy(PostStatus status) {
        Assert.notNull(status, "Post status must not be null");

        return postRepository.findAllByStatus(status);
    }

    @Override
    public Post filterIfEncrypt(Post post) {
        Assert.notNull(post, "Post must not be null");

        if (StringUtils.isNotBlank(post.getPassword())) {
            String tip = "The post is encrypted by author";
            post.setSummary(tip);
            post.setOriginalContent(tip);
            post.setFormatContent(tip);
        }

        return post;
    }

    @Override
    public Optional<Post> getPrePost(Date createTime) {
        Assert.notNull(createTime, "Create time must not be null");

        Page<Post> prePostPage = postRepository.findAllByStatusAndCreateTimeAfter(PostStatus.PUBLISHED, createTime, PageRequest.of(0, 1));

        if (prePostPage.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(prePostPage.getContent().get(0));
    }

    @Override
    public Optional<Post> getNextPost(Date createTime) {
        Assert.notNull(createTime, "Create time must not be null");

        Page<Post> nextPostPage = postRepository.findAllByStatusAndCreateTimeBefore(PostStatus.PUBLISHED, createTime, PageRequest.of(0, 1));

        if (nextPostPage.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(nextPostPage.getContent().get(nextPostPage.getContent().size()-1));
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
        return posts.stream()
                .map(post -> new PostMinimalOutputDTO().<PostMinimalOutputDTO>convertFrom(post))
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
