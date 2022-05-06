package run.halo.app.service.impl;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.exception.AlreadyExistsException;
import run.halo.app.exception.BadRequestException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.exception.ServiceException;
import run.halo.app.model.dto.post.BasePostSimpleDTO;
import run.halo.app.model.entity.BasePost;
import run.halo.app.model.entity.Content;
import run.halo.app.model.entity.Content.PatchedContent;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.properties.PostProperties;
import run.halo.app.repository.base.BasePostRepository;
import run.halo.app.service.ContentPatchLogService;
import run.halo.app.service.ContentService;
import run.halo.app.service.OptionService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.service.base.BasePostService;
import run.halo.app.utils.DateUtils;
import run.halo.app.utils.HaloUtils;
import run.halo.app.utils.ServiceUtils;

/**
 * Base post service implementation.
 *
 * @author johnniang
 * @author ryanwang
 * @author guqing
 * @date 2019-04-24
 */
@Slf4j
public abstract class BasePostServiceImpl<POST extends BasePost>
    extends AbstractCrudService<POST, Integer> implements BasePostService<POST> {

    private final BasePostRepository<POST> basePostRepository;

    private final OptionService optionService;

    private final ContentService contentService;

    private final ContentPatchLogService contentPatchLogService;

    private static final Pattern summaryPattern = Pattern.compile("\t|\r|\n");

    private static final Pattern BLANK_PATTERN = Pattern.compile("\\s");

    private static final String CHINESE_REGEX = "[^\\x00-\\xff]";

    private static final String PUNCTUATION_REGEX = "[\\p{P}\\p{S}\\p{Z}\\s]+";

    public BasePostServiceImpl(BasePostRepository<POST> basePostRepository,
        OptionService optionService,
        ContentService contentService,
        ContentPatchLogService contentPatchLogService) {
        super(basePostRepository);
        this.basePostRepository = basePostRepository;
        this.optionService = optionService;
        this.contentService = contentService;
        this.contentPatchLogService = contentPatchLogService;
    }

    @Override
    public long countVisit() {
        return Optional.ofNullable(basePostRepository.countVisit()).orElse(0L);
    }

    @Override
    public long countLike() {
        return Optional.ofNullable(basePostRepository.countLike()).orElse(0L);
    }

    @Override
    public long countByStatus(PostStatus status) {
        Assert.notNull(status, "Post status must not be null");

        return basePostRepository.countByStatus(status);
    }

    @Override
    public POST getBySlug(String slug) {
        Assert.hasText(slug, "Slug must not be blank");

        return basePostRepository.getBySlug(slug)
            .orElseThrow(() -> new NotFoundException("查询不到该文章的信息").setErrorData(slug));
    }

    @Override
    public POST getBy(PostStatus status, String slug) {
        Assert.notNull(status, "Post status must not be null");
        Assert.hasText(slug, "Post slug must not be blank");

        Optional<POST> postOptional = basePostRepository.getBySlugAndStatus(slug, status);

        return postOptional
            .orElseThrow(() -> new NotFoundException("查询不到该文章的信息").setErrorData(slug));
    }

    @Override
    public POST getBy(PostStatus status, Integer id) {
        Assert.notNull(status, "Post status must not be null");
        Assert.notNull(id, "Post id must not be null");

        Optional<POST> postOptional = basePostRepository.getByIdAndStatus(id, status);

        return postOptional.orElseThrow(() -> new NotFoundException("查询不到该文章的信息").setErrorData(id));
    }

    @Override
    public PatchedContent getLatestContentById(Integer id) {
        return contentPatchLogService.getByPostId(id);
    }

    @Override
    public List<POST> listAllBy(PostStatus status) {
        Assert.notNull(status, "Post status must not be null");

        return basePostRepository.findAllByStatus(status);
    }


    @Override
    public List<POST> listPrevPosts(POST post, int size) {
        Assert.notNull(post, "Post must not be null");

        String indexSort =
            optionService.getByPropertyOfNonNull(PostProperties.INDEX_SORT).toString();

        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(ASC, indexSort));

        switch (indexSort) {
            case "createTime":
                return basePostRepository
                    .findAllByStatusAndCreateTimeAfter(PostStatus.PUBLISHED, post.getCreateTime(),
                        pageRequest).getContent();
            case "editTime":
                return basePostRepository
                    .findAllByStatusAndEditTimeAfter(PostStatus.PUBLISHED, post.getEditTime(),
                        pageRequest).getContent();
            case "visits":
                return basePostRepository
                    .findAllByStatusAndVisitsAfter(PostStatus.PUBLISHED, post.getVisits(),
                        pageRequest).getContent();
            default:
                return Collections.emptyList();
        }
    }

    @Override
    public List<POST> listNextPosts(POST post, int size) {
        Assert.notNull(post, "Post must not be null");

        String indexSort =
            optionService.getByPropertyOfNonNull(PostProperties.INDEX_SORT).toString();

        PageRequest pageRequest = PageRequest.of(0, size, Sort.by(DESC, indexSort));

        switch (indexSort) {
            case "createTime":
                return basePostRepository
                    .findAllByStatusAndCreateTimeBefore(PostStatus.PUBLISHED, post.getCreateTime(),
                        pageRequest).getContent();
            case "editTime":
                return basePostRepository
                    .findAllByStatusAndEditTimeBefore(PostStatus.PUBLISHED, post.getEditTime(),
                        pageRequest).getContent();
            case "visits":
                return basePostRepository
                    .findAllByStatusAndVisitsBefore(PostStatus.PUBLISHED, post.getVisits(),
                        pageRequest).getContent();
            default:
                return Collections.emptyList();
        }
    }

    @Override
    public Optional<POST> getPrevPost(POST post) {
        List<POST> posts = listPrevPosts(post, 1);

        return CollectionUtils.isEmpty(posts) ? Optional.empty() : Optional.of(posts.get(0));
    }

    @Override
    public Optional<POST> getNextPost(POST post) {
        List<POST> posts = listNextPosts(post, 1);

        return CollectionUtils.isEmpty(posts) ? Optional.empty() : Optional.of(posts.get(0));
    }

    @Override
    public Page<POST> pageLatest(int top) {
        Assert.isTrue(top > 0, "Top number must not be less than 0");

        PageRequest latestPageable = PageRequest.of(0, top, Sort.by(DESC, "createTime"));

        return listAll(latestPageable);
    }

    /**
     * Lists latest posts.
     *
     * @param top top number must not be less than 0
     * @return latest posts
     */
    @Override
    public List<POST> listLatest(int top) {
        Assert.isTrue(top > 0, "Top number must not be less than 0");

        PageRequest latestPageable = PageRequest.of(0, top, Sort.by(DESC, "createTime"));
        return basePostRepository.findAllByStatus(PostStatus.PUBLISHED, latestPageable)
            .getContent();
    }

    @Override
    public Page<POST> pageBy(Pageable pageable) {
        Assert.notNull(pageable, "Page info must not be null");

        return listAll(pageable);
    }


    @Override
    public Page<POST> pageBy(PostStatus status, Pageable pageable) {
        Assert.notNull(status, "Post status must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        return basePostRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional
    public void increaseVisit(long visits, Integer postId) {
        Assert.isTrue(visits > 0, "Visits to increase must not be less than 1");
        Assert.notNull(postId, "Post id must not be null");

        boolean finishedIncrease;
        if (basePostRepository.getByIdAndStatus(postId, PostStatus.DRAFT).isPresent()) {
            finishedIncrease = true;
            log.info("Post with id: [{}] is a draft and visits will not be updated", postId);
        } else {
            finishedIncrease = basePostRepository.updateVisit(visits, postId) == 1;
        }

        if (!finishedIncrease) {
            log.error("Post with id: [{}] may not be found", postId);
            throw new BadRequestException(
                "Failed to increase visits " + visits + " for post with id " + postId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseVisit(Integer postId) {
        increaseVisit(1L, postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseLike(long likes, Integer postId) {
        Assert.isTrue(likes > 0, "Likes to increase must not be less than 1");
        Assert.notNull(postId, "Post id must not be null");

        long affectedRows = basePostRepository.updateLikes(likes, postId);

        if (affectedRows != 1) {
            log.error("Post with id: [{}] may not be found", postId);
            throw new BadRequestException(
                "Failed to increase likes " + likes + " for post with id " + postId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseLike(Integer postId) {
        increaseLike(1L, postId);
    }

    /**
     * @param post post for article
     * @return post with handled data
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public POST createOrUpdateBy(POST post) {
        Assert.notNull(post, "Post must not be null");
        PatchedContent postContent = post.getContent();
        // word count stat
        post.setWordCount(htmlFormatWordCount(postContent.getContent()));
        POST savedPost;
        // Create or update post
        if (ServiceUtils.isEmptyId(post.getId())) {
            // The sheet will be created
            savedPost = create(post);
            contentService.createOrUpdateDraftBy(post.getId(),
                postContent.getContent(), postContent.getOriginalContent());
        } else {
            // The sheet will be updated
            // Set edit time
            post.setEditTime(DateUtils.now());
            contentService.createOrUpdateDraftBy(post.getId(),
                postContent.getContent(), postContent.getOriginalContent());
            // Update it
            savedPost = update(post);
        }

        if (PostStatus.PUBLISHED.equals(post.getStatus())
            || PostStatus.INTIMATE.equals(post.getStatus())) {
            contentService.publishContent(post.getId());
        }
        return savedPost;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public POST updateDraftContent(String content, String originalContent, Integer postId) {
        Assert.isTrue(!ServiceUtils.isEmptyId(postId), "Post id must not be empty");

        if (originalContent == null) {
            originalContent = "";
        }

        contentService.createOrUpdateDraftBy(postId, content, originalContent);

        POST post = getById(postId);
        post.setContent(getLatestContentById(postId));

        return post;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public POST updateStatus(PostStatus status, Integer postId) {
        Assert.notNull(status, "Post status must not be null");
        Assert.isTrue(!ServiceUtils.isEmptyId(postId), "Post id must not be empty");

        // Get post
        POST post = getById(postId);

        if (!status.equals(post.getStatus())) {
            // Update post
            int updatedRows = basePostRepository.updateStatus(status, postId);
            if (updatedRows != 1) {
                throw new ServiceException(
                    "Failed to update post status of post with id " + postId);
            }

            post.setStatus(status);
        }

        // Sync content
        if (PostStatus.PUBLISHED.equals(status)) {
            // If publish this post, then convert the formatted content
            Content postContent = contentService.publishContent(postId);
            post.setContent(PatchedContent.of(postContent));
        }

        return post;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<POST> updateStatusByIds(List<Integer> ids, PostStatus status) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return ids.stream().map(id -> {
            return updateStatus(status, id);
        }).collect(Collectors.toList());
    }

    @Override
    public String generateDescription(@Nullable String content) {
        if (StringUtils.isBlank(content)) {
            return StringUtils.EMPTY;
        }

        String text = HaloUtils.cleanHtmlTag(content);

        Matcher matcher = summaryPattern.matcher(text);
        text = matcher.replaceAll("");

        // Get summary length
        Integer summaryLength =
            optionService.getByPropertyOrDefault(PostProperties.SUMMARY_LENGTH, Integer.class, 150);

        return StringUtils.substring(text, 0, summaryLength);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public POST create(POST post) {
        // Check title
        slugMustNotExist(post);

        return super.create(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public POST update(POST post) {
        // Check title
        slugMustNotExist(post);

        return super.update(post);
    }

    @Override
    public Content getContentById(Integer postId) {
        Assert.notNull(postId, "The postId must not be null.");
        return contentService.getById(postId);
    }

    /**
     * Check if the slug is exist.
     *
     * @param post post must not be null
     */
    protected void slugMustNotExist(@NonNull POST post) {
        Assert.notNull(post, "Post must not be null");

        // Get slug count
        boolean exist;

        if (ServiceUtils.isEmptyId(post.getId())) {
            // The sheet will be created
            exist = basePostRepository.existsBySlug(post.getSlug());
        } else {
            // The sheet will be updated
            exist = basePostRepository.existsByIdNotAndSlug(post.getId(), post.getSlug());
        }

        if (exist) {
            throw new AlreadyExistsException("文章别名 " + post.getSlug() + " 已存在");
        }
    }

    @NonNull
    protected String generateSummary(@Nullable String htmlContent) {
        if (StringUtils.isBlank(htmlContent)) {
            return StringUtils.EMPTY;
        }

        String text = HaloUtils.cleanHtmlTag(htmlContent);

        Matcher matcher = summaryPattern.matcher(text);
        text = matcher.replaceAll("");

        // Get summary length
        Integer summaryLength =
            optionService.getByPropertyOrDefault(PostProperties.SUMMARY_LENGTH, Integer.class, 150);

        return StringUtils.substring(text, 0, summaryLength);
    }

    protected <T extends BasePostSimpleDTO> void generateAndSetSummaryIfAbsent(POST post,
        T postVo) {
        Assert.notNull(post, "The post must not be null.");
        if (StringUtils.isNotBlank(postVo.getSummary())) {
            return;
        }

        PatchedContent patchedContent = post.getContentOfNullable();
        if (patchedContent == null) {
            Content postContent = getContentById(post.getId());
            postVo.setSummary(generateSummary(postContent.getContent()));
        } else {
            postVo.setSummary(generateSummary(patchedContent.getContent()));
        }
    }

    // CS304 issue link : https://github.com/halo-dev/halo/issues/1759

    /**
     * @param htmlContent the markdown style content
     * @return word count except space and line separator
     */

    public static long htmlFormatWordCount(String htmlContent) {
        if (htmlContent == null) {
            return 0;
        }

        String cleanContent = HaloUtils.cleanHtmlTag(htmlContent);

        String tempString = cleanContent.replaceAll(CHINESE_REGEX, "");

        String otherString = cleanContent.replaceAll(CHINESE_REGEX, " ");

        int chineseWordCount = cleanContent.length() - tempString.length();

        String[] otherWords = otherString.split(PUNCTUATION_REGEX);

        int otherWordLength = otherWords.length;

        if (otherWordLength > 0 && otherWords[0].length() == 0) {
            otherWordLength--;
        }

        if (otherWords.length > 1 && otherWords[otherWords.length - 1].length() == 0) {
            otherWordLength--;
        }

        return chineseWordCount + otherWordLength;
    }

    /**
     * @param htmlContent the markdown style content
     * @return character count except space and line separator
     */

    public static long htmlFormatCharacterCount(String htmlContent) {
        if (htmlContent == null) {
            return 0;
        }

        String cleanContent = HaloUtils.cleanHtmlTag(htmlContent);

        Matcher matcher = BLANK_PATTERN.matcher(cleanContent);

        int count = 0;

        while (matcher.find()) {
            count++;
        }

        return cleanContent.length() - count;
    }
}
