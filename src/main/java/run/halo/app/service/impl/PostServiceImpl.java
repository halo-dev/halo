package run.halo.app.service.impl;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.event.logger.LogEvent;
import run.halo.app.event.post.PostUpdatedEvent;
import run.halo.app.event.post.PostVisitEvent;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.entity.Category;
import run.halo.app.model.entity.Content;
import run.halo.app.model.entity.Content.PatchedContent;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.PostCategory;
import run.halo.app.model.entity.PostComment;
import run.halo.app.model.entity.PostMeta;
import run.halo.app.model.entity.PostTag;
import run.halo.app.model.entity.Tag;
import run.halo.app.model.enums.LogType;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.params.PostParam;
import run.halo.app.model.params.PostQuery;
import run.halo.app.model.properties.PostProperties;
import run.halo.app.model.vo.ArchiveMonthVO;
import run.halo.app.model.vo.ArchiveYearVO;
import run.halo.app.model.vo.PostDetailVO;
import run.halo.app.model.vo.PostMarkdownVO;
import run.halo.app.repository.PostRepository;
import run.halo.app.repository.base.BasePostRepository;
import run.halo.app.service.CategoryService;
import run.halo.app.service.ContentPatchLogService;
import run.halo.app.service.ContentService;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostCategoryService;
import run.halo.app.service.PostCommentService;
import run.halo.app.service.PostMetaService;
import run.halo.app.service.PostService;
import run.halo.app.service.PostTagService;
import run.halo.app.service.TagService;
import run.halo.app.service.assembler.PostAssembler;
import run.halo.app.utils.DateUtils;
import run.halo.app.utils.HaloUtils;
import run.halo.app.utils.MarkdownUtils;
import run.halo.app.utils.ServiceUtils;
import run.halo.app.utils.SlugUtils;

/**
 * Post service implementation.
 *
 * @author johnniang
 * @author ryanwang
 * @author guqing
 * @author evanwang
 * @author coor.top
 * @author Raremaa
 * @date 2019-03-14
 */
@Slf4j
@Service
public class PostServiceImpl extends BasePostServiceImpl<Post> implements PostService {

    private final PostAssembler postAssembler;

    private final PostRepository postRepository;

    private final TagService tagService;

    private final CategoryService categoryService;

    private final PostTagService postTagService;

    private final ContentService postContentService;

    private final PostCategoryService postCategoryService;

    private final PostCommentService postCommentService;

    private final ApplicationEventPublisher eventPublisher;

    private final PostMetaService postMetaService;

    private final OptionService optionService;

    private final ContentPatchLogService postContentPatchLogService;

    private final ApplicationContext applicationContext;

    public PostServiceImpl(BasePostRepository<Post> basePostRepository,
        PostAssembler postAssembler, OptionService optionService,
        PostRepository postRepository,
        TagService tagService,
        CategoryService categoryService,
        PostTagService postTagService,
        PostCategoryService postCategoryService,
        PostCommentService postCommentService,
        ApplicationEventPublisher eventPublisher,
        PostMetaService postMetaService,
        ContentService contentService,
        ContentPatchLogService contentPatchLogService,
        ApplicationContext applicationContext) {
        super(basePostRepository, optionService, contentService, contentPatchLogService);
        this.postAssembler = postAssembler;
        this.postRepository = postRepository;
        this.tagService = tagService;
        this.categoryService = categoryService;
        this.postTagService = postTagService;
        this.postCategoryService = postCategoryService;
        this.postCommentService = postCommentService;
        this.eventPublisher = eventPublisher;
        this.postMetaService = postMetaService;
        this.optionService = optionService;
        this.postContentService = contentService;
        this.postContentPatchLogService = contentPatchLogService;
        this.applicationContext = applicationContext;
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
        postQuery.setStatuses(Set.of(PostStatus.PUBLISHED));

        // Build specification and find all
        return postRepository.findAll(buildSpecByQuery(postQuery), pageable);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDetailVO createBy(Post postToCreate, Set<Integer> tagIds, Set<Integer> categoryIds,
        Set<PostMeta> metas, boolean autoSave) {
        PostDetailVO createdPost = createOrUpdate(postToCreate, tagIds, categoryIds, metas);
        if (!autoSave) {
            // Log the creation
            LogEvent logEvent = new LogEvent(this, createdPost.getId().toString(),
                LogType.POST_PUBLISHED, createdPost.getTitle());
            eventPublisher.publishEvent(logEvent);
        }
        return createdPost;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDetailVO createBy(Post postToCreate, Set<Integer> tagIds, Set<Integer> categoryIds,
        boolean autoSave) {
        PostDetailVO createdPost = createOrUpdate(postToCreate, tagIds, categoryIds, null);
        if (!autoSave) {
            // Log the creation
            LogEvent logEvent = new LogEvent(this, createdPost.getId().toString(),
                LogType.POST_PUBLISHED, createdPost.getTitle());
            eventPublisher.publishEvent(logEvent);
        }
        return createdPost;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostDetailVO updateBy(Post postToUpdate, Set<Integer> tagIds, Set<Integer> categoryIds,
        Set<PostMeta> metas, boolean autoSave) {
        PostDetailVO updatedPost = createOrUpdate(postToUpdate, tagIds, categoryIds, metas);
        if (!autoSave) {
            // Log the creation
            LogEvent logEvent = new LogEvent(this, updatedPost.getId().toString(),
                LogType.POST_EDITED, updatedPost.getTitle());
            eventPublisher.publishEvent(logEvent);
        }
        return updatedPost;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Post updateStatus(PostStatus status, Integer postId) {
        Post post = super.updateStatus(status, postId);
        eventPublisher.publishEvent(new PostUpdatedEvent(this, post));
        return post;
    }

    @Override
    public Post getBy(PostStatus status, String slug) {
        return super.getBy(status, slug);
    }

    @Override
    public Post getBy(Integer year, Integer month, String slug) {
        Assert.notNull(year, "Post create year must not be null");
        Assert.notNull(month, "Post create month must not be null");
        Assert.notNull(slug, "Post slug must not be null");

        Optional<Post> postOptional = postRepository.findBy(year, month, slug);

        return postOptional
            .orElseThrow(() -> new NotFoundException("查询不到该文章的信息").setErrorData(slug));
    }

    @NonNull
    @Override
    public Post getBy(@NonNull Integer year, @NonNull String slug) {
        Assert.notNull(year, "Post create year must not be null");
        Assert.notNull(slug, "Post slug must not be null");

        Optional<Post> postOptional = postRepository.findBy(year, slug);

        return postOptional
            .orElseThrow(() -> new NotFoundException("查询不到该文章的信息").setErrorData(slug));
    }

    @Override
    public Post getBy(Integer year, Integer month, String slug, PostStatus status) {
        Assert.notNull(year, "Post create year must not be null");
        Assert.notNull(month, "Post create month must not be null");
        Assert.notNull(slug, "Post slug must not be null");
        Assert.notNull(status, "Post status must not be null");

        Optional<Post> postOptional = postRepository.findBy(year, month, slug, status);

        return postOptional
            .orElseThrow(() -> new NotFoundException("查询不到该文章的信息").setErrorData(slug));
    }

    @Override
    public Post getBy(Integer year, Integer month, Integer day, String slug) {
        Assert.notNull(year, "Post create year must not be null");
        Assert.notNull(month, "Post create month must not be null");
        Assert.notNull(day, "Post create day must not be null");
        Assert.notNull(slug, "Post slug must not be null");

        Optional<Post> postOptional = postRepository.findBy(year, month, day, slug);

        return postOptional
            .orElseThrow(() -> new NotFoundException("查询不到该文章的信息").setErrorData(slug));
    }

    @Override
    public Post getBy(Integer year, Integer month, Integer day, String slug, PostStatus status) {
        Assert.notNull(year, "Post create year must not be null");
        Assert.notNull(month, "Post create month must not be null");
        Assert.notNull(day, "Post create day must not be null");
        Assert.notNull(slug, "Post slug must not be null");
        Assert.notNull(status, "Post status must not be null");

        Optional<Post> postOptional = postRepository.findBy(year, month, day, slug, status);

        return postOptional
            .orElseThrow(() -> new NotFoundException("查询不到该文章的信息").setErrorData(slug));
    }

    @Override
    public PatchedContent getLatestContentById(Integer id) {
        return postContentPatchLogService.getByPostId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Post> removeByIds(Collection<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return ids.stream().map(this::removeById).collect(Collectors.toList());
    }

    @Override
    public Post getBySlug(String slug) {
        return super.getBySlug(slug);
    }

    @Override
    public Post getWithLatestContentById(Integer postId) {
        Post post = getById(postId);
        Content postContent = getContentById(postId);
        // Use the head pointer stored in the post content.
        PatchedContent patchedContent =
            postContentPatchLogService.getPatchedContentById(postContent.getHeadPatchLogId());
        post.setContent(patchedContent);
        return post;
    }

    @Override
    public List<ArchiveYearVO> listYearArchives() {
        // Get all posts
        List<Post> posts = postRepository
            .findAllByStatus(PostStatus.PUBLISHED, Sort.by(DESC, "createTime"));

        return postAssembler.convertToYearArchives(posts);
    }

    @Override
    public List<ArchiveMonthVO> listMonthArchives() {
        // Get all posts
        List<Post> posts = postRepository
            .findAllByStatus(PostStatus.PUBLISHED, Sort.by(DESC, "createTime"));

        return postAssembler.convertToMonthArchives(posts);
    }

    @Override
    public PostDetailVO importMarkdown(String markdown, String filename) {
        Assert.notNull(markdown, "Markdown document must not be null");

        // Gets frontMatter
        Map<String, List<String>> frontMatter = MarkdownUtils.getFrontMatter(markdown);
        // remove frontMatter
        markdown = MarkdownUtils.removeFrontMatter(markdown);

        PostParam post = new PostParam();
        post.setStatus(null);

        List<String> elementValue;

        Set<Integer> tagIds = new HashSet<>();

        Set<Integer> categoryIds = new HashSet<>();

        if (frontMatter.size() > 0) {
            for (String key : frontMatter.keySet()) {
                elementValue = frontMatter.get(key);
                for (String ele : elementValue) {
                    ele = HaloUtils.strip(ele, "[", "]");
                    ele = StringUtils.strip(ele, "\"");
                    ele = StringUtils.strip(ele, "\'");
                    if ("".equals(ele)) {
                        continue;
                    }
                    switch (key) {
                        case "title":
                            post.setTitle(ele);
                            break;
                        case "date":
                            post.setCreateTime(DateUtils.parseDate(ele));
                            break;
                        case "permalink":
                            post.setSlug(ele);
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
                            Tag tag;
                            for (String tagName : ele.split(",")) {
                                tagName = tagName.trim();
                                tagName = StringUtils.strip(tagName, "\"");
                                tagName = StringUtils.strip(tagName, "\'");
                                tag = tagService.getByName(tagName);
                                String slug = SlugUtils.slug(tagName);
                                if (null == tag) {
                                    tag = tagService.getBySlug(slug);
                                }
                                if (null == tag) {
                                    tag = new Tag();
                                    tag.setName(tagName);
                                    tag.setSlug(slug);
                                    tag = tagService.create(tag);
                                }
                                tagIds.add(tag.getId());
                            }
                            break;
                        case "categories":
                            Integer lastCategoryId = null;
                            for (String categoryName : ele.split(",")) {
                                categoryName = categoryName.trim();
                                categoryName = StringUtils.strip(categoryName, "\"");
                                categoryName = StringUtils.strip(categoryName, "\'");
                                Category category = categoryService.getByName(categoryName);
                                if (null == category) {
                                    category = new Category();
                                    category.setName(categoryName);
                                    category.setSlug(SlugUtils.slug(categoryName));
                                    category.setDescription(categoryName);
                                    if (lastCategoryId != null) {
                                        category.setParentId(lastCategoryId);
                                    }
                                    category = categoryService.create(category);
                                }
                                lastCategoryId = category.getId();
                                categoryIds.add(lastCategoryId);
                            }
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

        if (StringUtils.isEmpty(post.getTitle())) {
            post.setTitle(filename);
        }

        if (StringUtils.isEmpty(post.getSlug())) {
            post.setSlug(SlugUtils.slug(post.getTitle()));
        }

        post.setOriginalContent(markdown);

        return createBy(post.convertTo(), tagIds, categoryIds, false);
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

        StringBuilder content = new StringBuilder("---\n");

        content.append("type: ").append("post").append("\n");
        content.append("title: ").append(post.getTitle()).append("\n");
        content.append("permalink: ").append(post.getSlug()).append("\n");
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

        List<Category> categories = postCategoryService.listCategoriesBy(post.getId());

        if (categories.size() > 0) {
            content.append("categories:").append("\n");
            for (Category category : categories) {
                content.append("  - ").append(category.getName()).append("\n");
            }
        }

        List<PostMeta> metas = postMetaService.listBy(post.getId());

        if (metas.size() > 0) {
            content.append("metas:").append("\n");
            for (PostMeta postMeta : metas) {
                content.append("  - ").append(postMeta.getKey()).append(" :  ")
                    .append(postMeta.getValue()).append("\n");
            }
        }

        content.append("---\n\n");
        PatchedContent postContent = post.getContent();
        content.append(postContent.getOriginalContent());
        return content.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Post removeById(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        log.debug("Removing post: [{}]", postId);

        // Remove post tags
        List<PostTag> postTags = postTagService.removeByPostId(postId);

        log.debug("Removed post tags: [{}]", postTags);

        // Remove post categories
        List<PostCategory> postCategories = postCategoryService.removeByPostId(postId);

        log.debug("Removed post categories: [{}]", postCategories);

        // Remove metas
        List<PostMeta> metas = postMetaService.removeByPostId(postId);
        log.debug("Removed post metas: [{}]", metas);

        // Remove post comments
        List<PostComment> postComments = postCommentService.removeByPostId(postId);
        log.debug("Removed post comments: [{}]", postComments);

        // Remove post content
        Content postContent = postContentService.removeById(postId);
        log.debug("Removed post content: [{}]", postContent);

        Post deletedPost = super.removeById(postId);
        deletedPost.setContent(PatchedContent.of(postContent));

        // Log it
        eventPublisher.publishEvent(new LogEvent(this, postId.toString(), LogType.POST_DELETED,
            deletedPost.getTitle()));

        return deletedPost;
    }

    /**
     * Build specification by post query.
     *
     * @param postQuery post query must not be null
     * @return a post specification
     */

    // CS304 issue: https://github.com/halo-dev/halo/issues/1842
    @NonNull
    private Specification<Post> buildSpecByQuery(@NonNull PostQuery postQuery) {
        Assert.notNull(postQuery, "Post query must not be null");

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new LinkedList<>();

            Set<PostStatus> statuses = postQuery.getStatuses();
            if (!CollectionUtils.isEmpty(statuses)) {
                predicates.add(root.get("status").in(statuses));
            }

            if (postQuery.getCategoryId() != null) {
                List<Integer> categoryIds =
                    categoryService.listAllByParentId(postQuery.getCategoryId())
                        .stream()
                        .map(Category::getId)
                        .collect(Collectors.toList());
                Subquery<Post> postSubquery = query.subquery(Post.class);
                Root<PostCategory> postCategoryRoot = postSubquery.from(PostCategory.class);
                postSubquery.select(postCategoryRoot.get("postId"));
                postSubquery.where(
                    criteriaBuilder.equal(root.get("id"), postCategoryRoot.get("postId")),
                    postCategoryRoot.get("categoryId").in(categoryIds));
                predicates.add(criteriaBuilder.exists(postSubquery));
            }

            if (postQuery.getKeyword() != null) {

                // Format like condition
                String likeCondition = String
                    .format("%%%s%%", StringUtils.strip(postQuery.getKeyword()));

                // Build like predicate
                Subquery<Post> postSubquery = query.subquery(Post.class);
                Root<Content> contentRoot = postSubquery.from(Content.class);
                postSubquery.select(contentRoot.get("id"))
                    .where(criteriaBuilder.like(contentRoot.get("originalContent"), likeCondition));

                Predicate titleLike = criteriaBuilder.like(root.get("title"), likeCondition);

                predicates.add(
                    criteriaBuilder.or(titleLike, criteriaBuilder.in(root).value(postSubquery)));
            }

            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };
    }

    private PostDetailVO createOrUpdate(@NonNull Post post, Set<Integer> tagIds,
        Set<Integer> categoryIds, Set<PostMeta> metas) {
        Assert.notNull(post, "Post param must not be null");

        post = super.createOrUpdateBy(post);

        postTagService.removeByPostId(post.getId());

        postCategoryService.removeByPostId(post.getId());

        // List all tags
        List<Tag> tags = tagService.listAllByIds(tagIds);

        // List all categories
        List<Category> categories = categoryService.listAllByIds(categoryIds);

        // Create post tags
        List<PostTag> postTags = postTagService.mergeOrCreateByIfAbsent(post.getId(),
            ServiceUtils.fetchProperty(tags, Tag::getId));

        log.debug("Created post tags: [{}]", postTags);

        // Create post categories
        List<PostCategory> postCategories =
            postCategoryService.mergeOrCreateByIfAbsent(post.getId(),
                ServiceUtils.fetchProperty(categories, Category::getId));

        log.debug("Created post categories: [{}]", postCategories);

        // Create post meta data
        List<PostMeta> postMetaList = postMetaService
            .createOrUpdateByPostId(post.getId(), metas);
        log.debug("Created post metas: [{}]", postMetaList);

        // Publish post updated event.
        applicationContext.publishEvent(new PostUpdatedEvent(this, post));

        // get draft content by head patch log id
        Content postContent = postContentService.getById(post.getId());
        post.setContent(
            postContentPatchLogService.getPatchedContentById(postContent.getHeadPatchLogId()));
        // Convert to post detail vo
        return postAssembler.convertTo(post, tags, categories, postMetaList);
    }

    @Override
    public void publishVisitEvent(Integer postId) {
        eventPublisher.publishEvent(new PostVisitEvent(this, postId));
    }

    @Override
    public @NotNull Sort getPostDefaultSort() {
        String indexSort = optionService.getByPropertyOfNonNull(PostProperties.INDEX_SORT)
            .toString();
        return Sort.by(DESC, "topPriority").and(Sort.by(DESC, indexSort).and(Sort.by(DESC, "id")));
    }

    @Override
    public List<PostMarkdownVO> listPostMarkdowns() {
        List<Post> allPostList = listAll();
        List<PostMarkdownVO> result = new ArrayList<>(allPostList.size());
        for (Post post : allPostList) {
            Content postContent = getContentById(post.getId());
            post.setContent(PatchedContent.of(postContent));
            result.add(convertToPostMarkdownVo(post));
        }
        return result;
    }

    private PostMarkdownVO convertToPostMarkdownVo(Post post) {
        PostMarkdownVO postMarkdownVO = new PostMarkdownVO();

        // set frontMatter
        StringBuilder frontMatter = getFrontMatterYaml(post);
        postMarkdownVO.setFrontMatter(frontMatter.toString());

        // set content
        PatchedContent postContent = post.getContent();
        postMarkdownVO.setOriginalContent(postContent.getOriginalContent());
        postMarkdownVO.setTitle(post.getTitle());
        postMarkdownVO.setSlug(post.getSlug());
        return postMarkdownVO;
    }

    /**
     * frontMatter has a variety of parsing methods, but the most commonly used is the yaml type.
     * yaml is used here, and public methods can be extracted if needed for extensions.
     * <p>
     * Example: <br>
     * title: default title. <br>
     * date: 2022-04-03 19:00:00.000 <br>
     * updated: 2022-04-03 19:00:00.000 <br>
     * description: default description <br>
     * categories: <br>
     * - Java <br>
     * - Halo <br>
     * tags: <br>
     * - tag <br>
     * - doc <br>
     * </p>
     *
     * @param post post not be null
     * @return frontMatter
     */
    private StringBuilder getFrontMatterYaml(Post post) {
        StringBuilder frontMatter = new StringBuilder("---\n");
        frontMatter.append("title: ").append(post.getTitle()).append("\n");
        frontMatter.append("date: ").append(post.getCreateTime()).append("\n");
        frontMatter.append("updated: ").append(post.getUpdateTime()).append("\n");

        // set fullPath
        frontMatter.append("url: ").append(postAssembler.buildFullPath(post)).append("\n");

        // set category
        // classification with hierarchies has not been processed yet
        List<Category> categories = postCategoryService.listCategoriesBy(post.getId());
        StringBuilder categoryContent = new StringBuilder();
        categories.forEach(category -> categoryContent.append("- ").append(category.getName())
            .append("\n"));
        frontMatter.append("categories: ").append("\n").append(categoryContent);

        // set tags
        List<Tag> tags = postTagService.listTagsBy(post.getId());
        StringBuilder tagContent = new StringBuilder();
        tags.forEach(tag -> tagContent.append("- ").append(tag.getName()).append("\n"));
        frontMatter.append("tags: ").append("\n").append(tagContent);

        frontMatter.append("---\n");
        return frontMatter;
    }
}
