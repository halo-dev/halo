package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.event.logger.LogEvent;
import run.halo.app.event.post.PostVisitEvent;
import run.halo.app.exception.AlreadyExistsException;
import run.halo.app.exception.BadRequestException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.CategoryDTO;
import run.halo.app.model.dto.TagDTO;
import run.halo.app.model.dto.post.PostMinimalDTO;
import run.halo.app.model.dto.post.PostSimpleDTO;
import run.halo.app.model.entity.*;
import run.halo.app.model.enums.LogType;
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

import static org.springframework.data.domain.Sort.Direction.ASC;
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

    private final ApplicationEventPublisher eventPublisher;

    public PostServiceImpl(PostRepository postRepository,
                           TagService tagService,
                           CategoryService categoryService,
                           PostTagService postTagService,
                           PostCategoryService postCategoryService,
                           CommentService commentService,
                           ApplicationEventPublisher eventPublisher) {
        super(postRepository);
        this.postRepository = postRepository;
        this.tagService = tagService;
        this.categoryService = categoryService;
        this.postTagService = postTagService;
        this.postCategoryService = postCategoryService;
        this.commentService = commentService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Page<PostMinimalDTO> pageLatestOfMinimal(int top) {
        return pageLatest(top).map(post -> new PostMinimalDTO().convertFrom(post));
    }

    @Override
    public Page<PostSimpleDTO> pageLatestOfSimple(int top) {
        return pageLatest(top).map(post -> new PostSimpleDTO().convertFrom(post));
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

    @Override
    public Page<Post> pageBy(String keyword, Pageable pageable) {
        Assert.notNull(keyword, "keyword must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        PostQuery postQuery = new PostQuery();
        postQuery.setKeyword(keyword);

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
     * @return Page<PostSimpleDTO>
     */
    @Override
    public Page<PostSimpleDTO> pageSimpleDtoByStatus(PostStatus status, Pageable pageable) {
        return pageBy(status, pageable).map(post -> new PostSimpleDTO().convertFrom(post));
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

    @Override
    public Post create(Post post) {
        Post createdPost = super.create(post);

        // Log the creation
        LogEvent logEvent = new LogEvent(this, createdPost.getId().toString(), LogType.POST_PUBLISHED, createdPost.getTitle());
        eventPublisher.publishEvent(logEvent);

        return createdPost;
    }

    @Override
    public Post update(Post post) {
        Post updatedPost = super.update(post);

        // Log the creation
        LogEvent logEvent = new LogEvent(this, updatedPost.getId().toString(), LogType.POST_EDITED, updatedPost.getTitle());
        eventPublisher.publishEvent(logEvent);

        return updatedPost;
    }

    private PostDetailVO createOrUpdate(@NonNull Post post, Set<Integer> tagIds, Set<Integer> categoryIds, @NonNull Function<Post, Post> postOperation) {
        Assert.notNull(post, "Post param must not be null");
        Assert.notNull(postOperation, "Post operation must not be null");

        // Check url
        long count;
        boolean isUpdating = post.getId() != null;
        if (isUpdating) {
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
    public Post getBy(PostStatus status, String url) {
        Assert.notNull(status, "Post status must not be null");
        Assert.hasText(url, "Post url must not be blank");

        Optional<Post> postOptional = postRepository.getByUrlAndStatus(url, status);

        Post post = postOptional.orElseThrow(() -> new NotFoundException("The post with status " + status + " and url " + url + "was not existed").setErrorData(url));

        if (PostStatus.PUBLISHED.equals(status)) {
            // Log it
            eventPublisher.publishEvent(new PostVisitEvent(this, post.getId()));
        }

        return post;
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
    public void increaseVisit(long visits, Integer postId) {
        Assert.isTrue(visits > 0, "Visits to increase must not be less than 1");
        Assert.notNull(postId, "Goods id must not be null");

        long affectedRows = postRepository.updateVisit(visits, postId);

        if (affectedRows != 1) {
            log.error("Post with id: [{}] may not be found", postId);
            throw new BadRequestException("Failed to increase visits " + visits + " for post with id " + postId);
        }
    }

    @Override
    public void increaseLike(long likes, Integer postId) {
        Assert.isTrue(likes > 0, "Likes to increase must not be less than 1");
        Assert.notNull(postId, "Goods id must not be null");

        long affectedRows = postRepository.updateLikes(likes, postId);

        if (affectedRows != 1) {
            log.error("Post with id: [{}] may not be found", postId);
            throw new BadRequestException("Failed to increase likes " + likes + " for post with id " + postId);
        }
    }

    @Override
    public void increaseVisit(Integer postId) {
        increaseVisit(1L, postId);
    }

    @Override
    public void increaseLike(Integer postId) {
        increaseLike(1L, postId);
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
    public Post removeById(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        log.debug("Removing post: [{}]", postId);

        // Remove post tags
        List<PostTag> postTags = postTagService.removeByPostId(postId);

        log.debug("Removed post tags: [{}]", postTags);

        // Remove post categories
        List<PostCategory> postCategories = postCategoryService.removeByPostId(postId);

        log.debug("Removed post categories: [{}]", postCategories);

        Post deletedPost = super.removeById(postId);

        // Log it
        eventPublisher.publishEvent(new LogEvent(this, postId.toString(), LogType.POST_DELETED, deletedPost.getTitle()));

        return deletedPost;
    }

    @Override
    public Page<PostSimpleDTO> convertToSimpleDto(@NonNull Page<Post> postPage) {
        Assert.notNull(postPage, "Post page must not be null");

        return postPage.map(post -> new PostSimpleDTO().convertFrom(post));
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
                    .filter(Objects::nonNull)
                    .map(tag -> new TagDTO().<TagDTO>convertFrom(tag))
                    .collect(Collectors.toList()));

            // Set categories
            postListVO.setCategories(Optional.ofNullable(categoryListMap.get(post.getId()))
                    .orElseGet(LinkedList::new)
                    .stream()
                    .filter(Objects::nonNull)
                    .map(category -> new CategoryDTO().<CategoryDTO>convertFrom(category))
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
    public Optional<Post> getPrePost(Date date) {
        List<Post> posts = listPrePosts(date, 1);

        return CollectionUtils.isEmpty(posts) ? Optional.empty() : Optional.of(posts.get(0));
    }

    @Override
    public Optional<Post> getNextPost(Date date) {
        List<Post> posts = listNextPosts(date, 1);

        return CollectionUtils.isEmpty(posts) ? Optional.empty() : Optional.of(posts.get(0));
    }

    @Override
    public List<Post> listPrePosts(Date date, int size) {
        Assert.notNull(date, "Date must not be null");

        return postRepository.findAllByStatusAndCreateTimeAfter(PostStatus.PUBLISHED,
                date,
                PageRequest.of(0, size, Sort.by(ASC, "createTime")))
                .getContent();
    }

    @Override
    public List<Post> listNextPosts(Date date, int size) {
        Assert.notNull(date, "Date must not be null");

        return postRepository.findAllByStatusAndCreateTimeBefore(PostStatus.PUBLISHED,
                date,
                PageRequest.of(0, size, Sort.by(DESC, "createTime")))
                .getContent();
    }

    /**
     * Converts to post minimal output dto.
     *
     * @param posts a list of post
     * @return a list of post minimal output dto
     */
    @NonNull
    private List<PostMinimalDTO> convertTo(@NonNull List<Post> posts) {
        if (CollectionUtils.isEmpty(posts)) {
            return Collections.emptyList();
        }

        // Convert
        return posts.stream()
                .map(post -> new PostMinimalDTO().<PostMinimalDTO>convertFrom(post))
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
