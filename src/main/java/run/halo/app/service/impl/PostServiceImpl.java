package run.halo.app.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import run.halo.app.event.logger.LogEvent;
import run.halo.app.event.post.PostVisitEvent;
import run.halo.app.model.dto.CategoryDTO;
import run.halo.app.model.dto.TagDTO;
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
import run.halo.app.utils.*;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Post service implementation.
 *
 * @author johnniang
 * @author ryanwang
 */
@Slf4j
@Service
public class PostServiceImpl extends BasePostServiceImpl<Post> implements PostService {

    private final PostRepository postRepository;

    private final TagService tagService;

    private final CategoryService categoryService;

    private final PostTagService postTagService;

    private final PostCategoryService postCategoryService;

    private final PostCommentService postCommentService;

    private final ApplicationEventPublisher eventPublisher;

    public PostServiceImpl(PostRepository postRepository,
                           TagService tagService,
                           CategoryService categoryService,
                           PostTagService postTagService,
                           PostCategoryService postCategoryService,
                           PostCommentService postCommentService,
                           ApplicationEventPublisher eventPublisher,
                           OptionService optionService) {
        super(postRepository, optionService);
        this.postRepository = postRepository;
        this.tagService = tagService;
        this.categoryService = categoryService;
        this.postTagService = postTagService;
        this.postCategoryService = postCategoryService;
        this.postCommentService = postCommentService;
        this.eventPublisher = eventPublisher;
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
        postQuery.setStatus(PostStatus.PUBLISHED);

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

    @Override
    public PostDetailVO createBy(Post postToCreate, Set<Integer> tagIds, Set<Integer> categoryIds, boolean autoSave) {
        PostDetailVO createdPost = createOrUpdate(postToCreate, tagIds, categoryIds);
        if (!autoSave) {
            // Log the creation
            LogEvent logEvent = new LogEvent(this, createdPost.getId().toString(), LogType.POST_PUBLISHED, createdPost.getTitle());
            eventPublisher.publishEvent(logEvent);
        }
        return createdPost;
    }

    @Override
    public PostDetailVO updateBy(Post postToUpdate, Set<Integer> tagIds, Set<Integer> categoryIds, boolean autoSave) {
        // Set edit time
        postToUpdate.setEditTime(DateUtils.now());
        PostDetailVO updatedPost = createOrUpdate(postToUpdate, tagIds, categoryIds);
        if (!autoSave) {
            // Log the creation
            LogEvent logEvent = new LogEvent(this, updatedPost.getId().toString(), LogType.POST_EDITED, updatedPost.getTitle());
            eventPublisher.publishEvent(logEvent);
        }
        return updatedPost;
    }

    private PostDetailVO createOrUpdate(@NonNull Post post, Set<Integer> tagIds, Set<Integer> categoryIds) {
        Assert.notNull(post, "Post param must not be null");

        // Create or update post
        post = super.createOrUpdateBy(post);

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

    @Override
    public Post getBy(PostStatus status, String url) {
        Post post = super.getBy(status, url);

        if (PostStatus.PUBLISHED.equals(status)) {
            // Log it
            eventPublisher.publishEvent(new PostVisitEvent(this, post.getId()));
        }

        return post;
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
            archive.setPosts(convertToMinimal(postList));

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
                    archive.setPosts(convertToMinimal(postList));

                    archives.add(archive);
                }));

        // Sort this list
        archives.sort(new ArchiveMonthVO.ArchiveComparator());

        return archives;
    }

    @Override
    public PostDetailVO importMarkdown(String markdown, String filename) {
        Assert.notNull(markdown, "Markdown document must not be null");

        // Render markdown to html document.
        String content = MarkdownUtils.renderMarkdown(markdown);

        // Gets frontMatter
        Map<String, List<String>> frontMatter = MarkdownUtils.getFrontMatter(markdown);

        Post post = new Post();

        List<String> elementValue;

        Set<Integer> tagIds = new HashSet<>();

        Set<Integer> categoryIds = new HashSet<>();
        if (frontMatter.size() > 0) {
            for (String key : frontMatter.keySet()) {
                elementValue = frontMatter.get(key);
                for (String ele : elementValue) {
                    switch (key) {
                        case "title":
                            post.setTitle(ele);
                            break;
                        case "date":
                            post.setCreateTime(DateUtil.parse(ele));
                            break;
                        case "updated":
                            post.setUpdateTime(DateUtil.parse(ele));
                            break;
                        case "permalink":
                            post.setUrl(ele);
                            break;
                        case "thumbnail":
                            post.setThumbnail(ele);
                            break;
                        case "status":
                            post.setStatus(PostStatus.valueOf(ele));
                            break;
                        case "comments":
                            post.setDisallowComment(Boolean.parseBoolean(ele));
                            break;
                        case "tags":
                            Tag tag = tagService.getByName(ele);
                            if (null == tag) {
                                tag = new Tag();
                                tag.setName(ele);
                                String slugName = SlugUtils.slugify(ele);
                                tag.setSlugName(HaloUtils.initializeUrlIfBlank(slugName));
                                tag = tagService.create(tag);
                            }
                            tagIds.add(tag.getId());
                            break;
                        case "categories":
                            Category category = categoryService.getByName(ele);
                            if (null == category) {
                                category = new Category();
                                category.setName(ele);
                                String slugName = SlugUtils.slugify(ele);
                                category.setSlugName(HaloUtils.initializeUrlIfBlank(slugName));
                                category.setDescription(ele);
                                category = categoryService.create(category);
                            }
                            categoryIds.add(category.getId());
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        if (null == post.getStatus()) {
            post.setStatus(PostStatus.PUBLISHED);
        }

        if (StrUtil.isEmpty(post.getTitle())) {
            post.setTitle(filename);
        }

        if (StrUtil.isEmpty(post.getUrl())) {
            post.setUrl(DateUtil.format(new Date(), "yyyyMMddHHmmss" + RandomUtil.randomNumbers(5)));
        }

        post.setOriginalContent(markdown);

        return createBy(post, tagIds, categoryIds, false);
    }

    @Override
    public String exportMarkdown(Integer id) {
        Assert.notNull(id, "Post id must not be null");
        Post post = getById(id);
        return exportMarkdown(post);
    }

    @Override
    public String exportMarkdown(Post post) {
        Assert.notNull(post, "Post must not be null");

        StrBuilder content = new StrBuilder("---\n");

        content.append("type: ").append("post").append("\n");
        content.append("title: ").append(post.getTitle()).append("\n");
        content.append("permalink: ").append(post.getUrl()).append("\n");
        content.append("thumbnail: ").append(post.getThumbnail()).append("\n");
        content.append("status: ").append(post.getStatus()).append("\n");
        content.append("date: ").append(post.getCreateTime()).append("\n");
        content.append("updated: ").append(post.getEditTime()).append("\n");
        content.append("comments: ").append(!post.getDisallowComment()).append("\n");

        List<Tag> tags = postTagService.listTagsBy(post.getId());

        if (tags.size() > 0) {
            content.append("tags:").append("\n");
            for (Tag tag : tags) {
                content.append("  - ").append(tag.getName()).append("\n");
            }
        }

        List<Category> categories = postCategoryService.listCategoryBy(post.getId());

        if (categories.size() > 0) {
            content.append("categories:").append("\n");
            for (Category category : categories) {
                content.append("  - ").append(category.getName()).append("\n");
            }
        }

        content.append("---\n\n");
        content.append(post.getOriginalContent());
        return content.toString();
    }

    @Override
    public PostDetailVO convertToDetailVo(Post post) {
        return convertTo(post,
                () -> postTagService.listTagIdsByPostId(post.getId()),
                () -> postCategoryService.listCategoryIdsByPostId(post.getId()));
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
    public Page<PostListVO> convertToListVo(Page<Post> postPage) {
        Assert.notNull(postPage, "Post page must not be null");

        List<Post> posts = postPage.getContent();

        Set<Integer> postIds = ServiceUtils.fetchProperty(posts, Post::getId);

        // Get tag list map
        Map<Integer, List<Tag>> tagListMap = postTagService.listTagListMapBy(postIds);

        // Get category list map
        Map<Integer, List<Category>> categoryListMap = postCategoryService.listCategoryListMap(postIds);

        // Get comment count
        Map<Integer, Long> commentCountMap = postCommentService.countByPostIds(postIds);

        return postPage.map(post -> {
            PostListVO postListVO = new PostListVO().convertFrom(post);

            if (StringUtils.isBlank(postListVO.getSummary())) {
                // Set summary
                postListVO.setSummary(convertToSummary(post.getOriginalContent()));
            }

            Optional.ofNullable(tagListMap.get(post.getId())).orElseGet(LinkedList::new);

            // Set tags
            postListVO.setTags(Optional.ofNullable(tagListMap.get(post.getId()))
                    .orElseGet(LinkedList::new)
                    .stream()
                    .filter(Objects::nonNull)
                    .map(tag -> (TagDTO) new TagDTO().convertFrom(tag))
                    .collect(Collectors.toList()));

            // Set categories
            postListVO.setCategories(Optional.ofNullable(categoryListMap.get(post.getId()))
                    .orElseGet(LinkedList::new)
                    .stream()
                    .filter(Objects::nonNull)
                    .map(category -> (CategoryDTO) new CategoryDTO().convertFrom(category))
                    .collect(Collectors.toList()));

            // Set comment count
            postListVO.setCommentCount(commentCountMap.getOrDefault(post.getId(), 0L));

            return postListVO;
        });
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
