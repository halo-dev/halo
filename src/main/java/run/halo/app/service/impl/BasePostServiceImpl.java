package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.exception.AlreadyExistsException;
import run.halo.app.exception.BadRequestException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.post.BasePostDetailDTO;
import run.halo.app.model.dto.post.BasePostMinimalDTO;
import run.halo.app.model.dto.post.BasePostSimpleDTO;
import run.halo.app.model.entity.BasePost;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.properties.PostProperties;
import run.halo.app.repository.base.BasePostRepository;
import run.halo.app.service.OptionService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.service.base.BasePostService;
import run.halo.app.utils.DateUtils;
import run.halo.app.utils.MarkdownUtils;
import run.halo.app.utils.ServiceUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Base post service implementation.
 *
 * @author johnniang
 * @date 19-4-24
 */
@Slf4j
public abstract class BasePostServiceImpl<POST extends BasePost> extends AbstractCrudService<POST, Integer> implements BasePostService<POST> {

    private final BasePostRepository<POST> basePostRepository;

    private final OptionService optionService;

    public BasePostServiceImpl(BasePostRepository<POST> basePostRepository,
                               OptionService optionService) {
        super(basePostRepository);
        this.basePostRepository = basePostRepository;
        this.optionService = optionService;
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
    public POST getByUrl(String url) {
        Assert.hasText(url, "Url must not be blank");

        return basePostRepository.getByUrl(url).orElseThrow(() -> new NotFoundException("The post does not exist").setErrorData(url));
    }

    @Override
    public POST getBy(PostStatus status, String url) {
        Assert.notNull(status, "Post status must not be null");
        Assert.hasText(url, "Post url must not be blank");

        Optional<POST> postOptional = basePostRepository.getByUrlAndStatus(url, status);

        return postOptional.orElseThrow(() -> new NotFoundException("The post with status " + status + " and url " + url + "was not existed").setErrorData(url));
    }

    @Override
    public List<POST> listAllBy(PostStatus status) {
        Assert.notNull(status, "Post status must not be null");

        return basePostRepository.findAllByStatus(status);
    }


    @Override
    public List<POST> listPrePosts(Date date, int size) {
        Assert.notNull(date, "Date must not be null");

        return basePostRepository.findAllByStatusAndCreateTimeAfter(PostStatus.PUBLISHED,
                date,
                PageRequest.of(0, size, Sort.by(ASC, "createTime")))
                .getContent();
    }

    @Override
    public List<POST> listNextPosts(Date date, int size) {
        Assert.notNull(date, "Date must not be null");

        return basePostRepository.findAllByStatusAndCreateTimeBefore(PostStatus.PUBLISHED,
                date,
                PageRequest.of(0, size, Sort.by(DESC, "createTime")))
                .getContent();
    }

    @Override
    public Optional<POST> getPrePost(Date date) {
        List<POST> posts = listPrePosts(date, 1);

        return CollectionUtils.isEmpty(posts) ? Optional.empty() : Optional.of(posts.get(0));
    }

    @Override
    public Optional<POST> getNextPost(Date date) {
        List<POST> posts = listNextPosts(date, 1);

        return CollectionUtils.isEmpty(posts) ? Optional.empty() : Optional.of(posts.get(0));
    }

    @Override
    public Page<POST> pageLatest(int top) {
        Assert.isTrue(top > 0, "Top number must not be less than 0");

        PageRequest latestPageable = PageRequest.of(0, top, Sort.by(DESC, "editTime"));

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

        PageRequest latestPageable = PageRequest.of(0, top, Sort.by(DESC, "editTime"));
        return basePostRepository.findAllByStatus(PostStatus.PUBLISHED, latestPageable).getContent();
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
    public void increaseVisit(long visits, Integer postId) {
        Assert.isTrue(visits > 0, "Visits to increase must not be less than 1");
        Assert.notNull(postId, "Goods id must not be null");

        long affectedRows = basePostRepository.updateVisit(visits, postId);

        if (affectedRows != 1) {
            log.error("Post with id: [{}] may not be found", postId);
            throw new BadRequestException("Failed to increase visits " + visits + " for post with id " + postId);
        }
    }

    @Override
    public void increaseLike(long likes, Integer postId) {
        Assert.isTrue(likes > 0, "Likes to increase must not be less than 1");
        Assert.notNull(postId, "Goods id must not be null");

        long affectedRows = basePostRepository.updateLikes(likes, postId);

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
    public POST createOrUpdateBy(POST post) {
        Assert.notNull(post, "Post must not be null");

        // Render content
        post.setFormatContent(MarkdownUtils.renderMarkdown(post.getOriginalContent()));

        // Create or update post
        if (ServiceUtils.isEmptyId(post.getId())) {
            // The sheet will be created
            return create(post);
        }

        // The sheet will be updated
        // Set edit time
        post.setEditTime(DateUtils.now());

        // Update it
        return update(post);
    }

    @Override
    public POST filterIfEncrypt(POST post) {
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
    public BasePostMinimalDTO convertToMinimal(POST post) {
        Assert.notNull(post, "Post must not be null");

        return new BasePostMinimalDTO().convertFrom(post);
    }

    @Override
    public List<BasePostMinimalDTO> convertToMinimal(List<POST> posts) {
        if (CollectionUtils.isEmpty(posts)) {
            return Collections.emptyList();
        }

        return posts.stream()
                .map(this::convertToMinimal)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BasePostMinimalDTO> convertToMinimal(Page<POST> postPage) {
        Assert.notNull(postPage, "Post page must not be null");

        return postPage.map(this::convertToMinimal);
    }

    @Override
    public BasePostSimpleDTO convertToSimple(POST post) {
        Assert.notNull(post, "Post must not be null");

        BasePostSimpleDTO basePostSimpleDTO = new BasePostSimpleDTO().convertFrom(post);

        // Set summary
        if (StringUtils.isBlank(basePostSimpleDTO.getSummary())) {
            basePostSimpleDTO.setSummary(convertToSummary(post.getOriginalContent()));
        }

        return basePostSimpleDTO;
    }

    @Override
    public List<BasePostSimpleDTO> convertToSimple(List<POST> posts) {
        if (CollectionUtils.isEmpty(posts)) {
            return Collections.emptyList();
        }

        return posts.stream()
                .map(this::convertToSimple)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BasePostSimpleDTO> convertToSimple(Page<POST> postPage) {
        Assert.notNull(postPage, "Post page must not be null");

        return postPage.map(this::convertToSimple);
    }

    @Override
    public BasePostDetailDTO convertToDetail(POST post) {
        Assert.notNull(post, "Post must not be null");

        return new BasePostDetailDTO().convertFrom(post);
    }

    @Override
    public POST create(POST post) {
        // Check title
        urlMustNotExist(post);

        return super.create(post);
    }

    @Override
    public POST update(POST post) {
        // Check title
        urlMustNotExist(post);

        return super.update(post);
    }

    /**
     * Check if the url is exist.
     *
     * @param post post must not be null
     */
    protected void urlMustNotExist(@NonNull POST post) {
        Assert.notNull(post, "Sheet must not be null");

        // Get url count
        boolean exist;

        if (ServiceUtils.isEmptyId(post.getId())) {
            // The sheet will be created
            exist = basePostRepository.existsByUrl(post.getUrl());
        } else {
            // The sheet will be updated
            exist = basePostRepository.existsByIdNotAndUrl(post.getId(), post.getUrl());
        }

        if (exist) {
            throw new AlreadyExistsException("The post url " + post.getUrl() + " has been exist");
        }
    }

    @NonNull
    protected String convertToSummary(@Nullable String markdownContent) {
        // Render text content
        String textContent = MarkdownUtils.renderText(markdownContent);

        // Get summary length
        Integer summaryLength = optionService.getByPropertyOrDefault(PostProperties.SUMMARY_LENGTH, Integer.class, 150);

        // Set summary
        return StringUtils.substring(textContent, 0, summaryLength);
    }

}
