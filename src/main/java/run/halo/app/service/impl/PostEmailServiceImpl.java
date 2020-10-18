package run.halo.app.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.EmailDTO;
import run.halo.app.model.dto.EmailWithPostCountDTO;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.PostEmail;
import run.halo.app.model.entity.Email;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.projection.EmailPostPostCountProjection;
import run.halo.app.repository.PostRepository;
import run.halo.app.repository.PostEmailRepository;
import run.halo.app.repository.EmailRepository;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostEmailService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.ServiceUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static run.halo.app.model.support.HaloConst.URL_SEPARATOR;

/**
 * Post email service implementation.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-19
 */
@Service
public class PostEmailServiceImpl extends AbstractCrudService<PostEmail, Integer> implements PostEmailService {

    private final PostEmailRepository postEmailRepository;

    private final PostRepository postRepository;

    private final EmailRepository emailRepository;

    private final OptionService optionService;

    public PostEmailServiceImpl(PostEmailRepository postEmailRepository,
                                PostRepository postRepository,
                                EmailRepository emailRepository,
                                OptionService optionService) {
        super(postEmailRepository);
        this.postEmailRepository = postEmailRepository;
        this.postRepository = postRepository;
        this.emailRepository = emailRepository;
        this.optionService = optionService;
    }

    @Override
    public List<Email> listEmailsBy(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        // Find all email ids
        Set<Integer> emailIds = postEmailRepository.findAllEmailIdsByPostId(postId);

        return emailRepository.findAllById(emailIds);
    }

    @Override
    public List<EmailDTO> listEmailWithCountDTOs(Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        // Find all emails
        List<Email> emails = emailRepository.findAll(sort);

        // Find all post count
        Map<Integer, Long> emailPostCountMap = ServiceUtils.convertToMap(postEmailRepository.findPostCount(), EmailPostPostCountProjection::getEmailId, EmailPostPostCountProjection::getPostCount);

        // Find post count
        return emails.stream().map(
                email -> {
                    EmailDTO emailDTO = new EmailDTO().convertFrom(email);

                    emailDTO.setPostCount(emailPostCountMap.getOrDefault(email.getId(), 0L));

                    StringBuilder fullPath = new StringBuilder();

                    if (optionService.isEnabledAbsolutePath()) {
                        fullPath.append(optionService.getBlogBaseUrl());
                    }

                    fullPath.append(URL_SEPARATOR)
                            .append(optionService.getTagsPrefix())
                            .append(URL_SEPARATOR)
                            .append(email.getId())
                            .append(optionService.getPathSuffix());

                    emailDTO.setFullPath(fullPath.toString());

                    return emailDTO;
                }
        ).collect(Collectors.toList());
    }

    @Override
    public Map<Integer, List<Email>> listEmailListMapBy(Collection<Integer> postIds) {
        if (CollectionUtils.isEmpty(postIds)) {
            return Collections.emptyMap();
        }

        // Find all post emails
        List<PostEmail> postEmails = postEmailRepository.findAllByPostIdIn(postIds);

        // Fetch email ids
        Set<Integer> emailIds = ServiceUtils.fetchProperty(postEmails, PostEmail::getEmailId);

        // Find all emails
        List<Email> emails = emailRepository.findAllById(emailIds);

        // Convert to email map
        Map<Integer, Email> emailMap = ServiceUtils.convertToMap(emails, Email::getId);

        // Create email list map
        Map<Integer, List<Email>> emailListMap = new HashMap<>();

        // Foreach and collect
        postEmails.forEach(postEmail -> emailListMap.computeIfAbsent(postEmail.getPostId(), postId -> new LinkedList<>()).add(emailMap.get(postEmail.getEmailId())));

        return emailListMap;
    }


    @Override
    public List<Post> listPostsBy(Integer emailId) {
        Assert.notNull(emailId, "Email id must not be null");

        // Find all post ids
        Set<Integer> postIds = postEmailRepository.findAllPostIdsByEmailId(emailId);

        return postRepository.findAllById(postIds);
    }

    @Override
    public List<Post> listPostsBy(Integer emailId, PostStatus status) {
        Assert.notNull(emailId, "Email id must not be null");
        Assert.notNull(status, "Post status must not be null");

        // Find all post ids
        Set<Integer> postIds = postEmailRepository.findAllPostIdsByEmailId(emailId, status);

        return postRepository.findAllById(postIds);
    }

    @Override
    public List<Post> listPostsBy(String slug, PostStatus status) {
        Assert.notNull(slug, "Email slug must not be null");
        Assert.notNull(status, "Post status must not be null");

        Email email = emailRepository.getByValue(slug).orElseThrow(() -> new NotFoundException("查询不到该邮箱的信息").setErrorData(slug));

        Set<Integer> postIds = postEmailRepository.findAllPostIdsByEmailId(email.getId(), status);

        return postRepository.findAllById(postIds);
    }

    @Override
    public Page<Post> pagePostsBy(Integer emailId, Pageable pageable) {
        Assert.notNull(emailId, "Email id must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        // Find all post ids
        Set<Integer> postIds = postEmailRepository.findAllPostIdsByEmailId(emailId);

        return postRepository.findAllByIdIn(postIds, pageable);
    }

    @Override
    public Page<Post> pagePostsBy(Integer emailId, PostStatus status, Pageable pageable) {
        Assert.notNull(emailId, "Email id must not be null");
        Assert.notNull(status, "Post status must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        // Find all post ids
        Set<Integer> postIds = postEmailRepository.findAllPostIdsByEmailId(emailId, status);

        return postRepository.findAllByIdIn(postIds, pageable);
    }

    @Override
    public List<PostEmail> mergeOrCreateByIfAbsent(Integer postId, List<Email> emails) {
        Assert.notNull(postId, "Post id must not be null");

        if (CollectionUtils.isEmpty(emails)) {
            return Collections.emptyList();
        }

        // Create post tags
        Set<Integer> emailIds = ServiceUtils.fetchProperty(emails, Email::getId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("查询不到该文章的信息").setErrorData(postId));

        // Convert to email map
        Map<Integer, Email> emailMap = ServiceUtils.convertToMap(emails, Email::getId);

        // Create post emails
        List<PostEmail> postEmails = emailIds.stream().map(emailId -> {
            // Build post email
            PostEmail postEmail = new PostEmail();
            postEmail.setPostId(post.getId());
            postEmail.setPostSlug(post.getSlug());
            postEmail.setPostTitle(post.getTitle());
            postEmail.setPostFormatContent(post.getFormatContent());
            postEmail.setPostOriginalContent(post.getOriginalContent());

            Email email = emailMap.get(emailId);
            postEmail.setEmailId(email.getId());
            postEmail.setEmailName(email.getName());
            postEmail.setEmailValue(email.getValue());
            return postEmail;
        }).collect(Collectors.toList());

        // Return post emails
        return postEmails;
    }

    @Override
    public List<PostEmail> listByPostId(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        return postEmailRepository.findAllByPostId(postId);
    }

    @Override
    public List<PostEmail> listByEmailId(Integer emailId) {
        Assert.notNull(emailId, "Email id must not be null");

        return postEmailRepository.findAllByEmailId(emailId);
    }

    @Override
    public Set<Integer> listEmailIdsByPostId(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        return postEmailRepository.findAllEmailIdsByPostId(postId);
    }

    @Override
    public List<PostEmail> removeByPostId(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        return postEmailRepository.deleteByPostId(postId);
    }

    @Override
    public List<PostEmail> removeByEmailId(Integer emailId) {
        Assert.notNull(emailId, "Email id must not be null");

        return postEmailRepository.deleteByEmailId(emailId);
    }
}
