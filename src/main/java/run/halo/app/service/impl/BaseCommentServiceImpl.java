package run.halo.app.service.impl;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.event.comment.CommentNewEvent;
import run.halo.app.event.comment.CommentReplyEvent;
import run.halo.app.exception.BadRequestException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.entity.BaseComment;
import run.halo.app.model.entity.User;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.params.BaseCommentParam;
import run.halo.app.model.params.CommentQuery;
import run.halo.app.model.projection.CommentChildrenCountProjection;
import run.halo.app.model.projection.CommentCountProjection;
import run.halo.app.model.properties.BlogProperties;
import run.halo.app.model.properties.CommentProperties;
import run.halo.app.model.vo.BaseCommentVO;
import run.halo.app.model.vo.BaseCommentWithParentVO;
import run.halo.app.model.vo.CommentWithHasChildrenVO;
import run.halo.app.repository.base.BaseCommentRepository;
import run.halo.app.security.authentication.Authentication;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.service.OptionService;
import run.halo.app.service.UserService;
import run.halo.app.service.assembler.comment.BaseCommentAssembler;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.service.base.BaseCommentService;
import run.halo.app.utils.HaloUtils;
import run.halo.app.utils.ServiceUtils;
import run.halo.app.utils.ServletUtils;
import run.halo.app.utils.ValidationUtils;

/**
 * Base comment service implementation.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-24
 */
@Slf4j
public abstract class BaseCommentServiceImpl<COMMENT extends BaseComment>
    extends AbstractCrudService<COMMENT, Long> implements BaseCommentService<COMMENT> {

    protected final OptionService optionService;
    protected final UserService userService;
    protected final ApplicationEventPublisher eventPublisher;
    private final BaseCommentRepository<COMMENT> baseCommentRepository;
    private final BaseCommentAssembler<COMMENT> commentAssembler;

    public BaseCommentServiceImpl(BaseCommentRepository<COMMENT> baseCommentRepository,
        OptionService optionService,
        UserService userService, ApplicationEventPublisher eventPublisher,
        BaseCommentAssembler<COMMENT> commentAssembler) {
        super(baseCommentRepository);
        this.baseCommentRepository = baseCommentRepository;
        this.optionService = optionService;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
        this.commentAssembler = commentAssembler;
    }

    @Override
    @NonNull
    public List<COMMENT> listBy(@NonNull Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        return baseCommentRepository.findAllByPostId(postId);
    }

    @Override
    @NonNull
    public Page<COMMENT> pageLatest(@NonNull int top) {
        return pageLatest(top, null);
    }

    @Override
    @NonNull
    public Page<COMMENT> pageLatest(int top, CommentStatus status) {
        if (status == null) {
            return listAll(ServiceUtils.buildLatestPageable(top));
        }

        return baseCommentRepository.findAllByStatus(status, ServiceUtils.buildLatestPageable(top));
    }

    @Override
    @NonNull
    public Page<COMMENT> pageBy(@NonNull CommentStatus status, @NonNull Pageable pageable) {

        Assert.notNull(status, "Comment status must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        // Find all
        return baseCommentRepository.findAllByStatus(status, pageable);
    }

    @Override
    @NonNull
    public Page<COMMENT> pageBy(@NonNull CommentQuery commentQuery, @NonNull Pageable pageable) {
        Assert.notNull(pageable, "Page info must not be null");

        return baseCommentRepository.findAll(buildSpecByQuery(commentQuery), pageable);
    }

    @Override
    @NonNull
    public Page<BaseCommentVO> pageVosAllBy(@NonNull Integer postId, @NonNull Pageable pageable) {
        Assert.notNull(postId, "Post id must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        log.debug("Getting comment tree view of post: [{}], page info: [{}]", postId, pageable);

        // List all the top comments (Caution: This list will be cleared)
        List<COMMENT> comments = baseCommentRepository.findAllByPostId(postId);

        return commentAssembler.pageVosBy(comments, pageable);
    }

    @Override
    @NonNull
    public Page<BaseCommentVO> pageVosBy(@NonNull Integer postId, @NonNull Pageable pageable) {
        Assert.notNull(postId, "Post id must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        log.debug("Getting comment tree view of post: [{}], page info: [{}]", postId, pageable);

        // List all the top comments (Caution: This list will be cleared)
        List<COMMENT> comments =
            baseCommentRepository.findAllByPostIdAndStatus(postId, CommentStatus.PUBLISHED);

        return commentAssembler.pageVosBy(comments, pageable);
    }

    @Override
    @NonNull
    public Page<BaseCommentWithParentVO> pageWithParentVoBy(@NonNull Integer postId,
        @NonNull Pageable pageable) {
        Assert.notNull(postId, "Post id must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        log.debug("Getting comment list view of post: [{}], page info: [{}]", postId, pageable);

        // List all the top comments (Caution: This list will be cleared)
        Page<COMMENT> commentPage = baseCommentRepository
            .findAllByPostIdAndStatus(postId, CommentStatus.PUBLISHED, pageable);

        // Get all comments
        List<COMMENT> comments = commentPage.getContent();

        // Get all comment parent ids
        Set<Long> parentIds = ServiceUtils.fetchProperty(comments, COMMENT::getParentId);

        // Get all parent comments
        List<COMMENT> parentComments =
            baseCommentRepository.findAllByIdIn(parentIds, pageable.getSort());

        // Convert to comment map (Key: comment id, value: comment)
        Map<Long, COMMENT> parentCommentMap =
            ServiceUtils.convertToMap(parentComments, COMMENT::getId);

        Map<Long, BaseCommentWithParentVO> parentCommentVoMap =
            new HashMap<>(parentCommentMap.size());

        // Convert to comment page
        return commentPage.map(comment -> {
            // Convert to with parent vo
            BaseCommentWithParentVO commentWithParentVo =
                new BaseCommentWithParentVO().convertFrom(comment);

            commentWithParentVo.setAvatar(
                commentAssembler.buildAvatarUrl(commentWithParentVo.getGravatarMd5()));

            // Get parent comment vo from cache
            BaseCommentWithParentVO parentCommentVo = parentCommentVoMap.get(comment.getParentId());

            if (parentCommentVo == null) {
                // Get parent comment
                COMMENT parentComment = parentCommentMap.get(comment.getParentId());

                if (parentComment != null) {
                    // Convert to parent comment vo
                    parentCommentVo = new BaseCommentWithParentVO().convertFrom(parentComment);

                    parentCommentVo.setAvatar(
                        commentAssembler.buildAvatarUrl(parentComment.getGravatarMd5()));

                    // Cache the parent comment vo
                    parentCommentVoMap.put(parentComment.getId(), parentCommentVo);
                }
            }

            // Set parent
            commentWithParentVo.setParent(parentCommentVo == null ? null : parentCommentVo.clone());

            return commentWithParentVo;
        });
    }

    @Override
    @NonNull
    public Map<Integer, Long> countByPostIds(Collection<Integer> postIds) {
        if (CollectionUtils.isEmpty(postIds)) {
            return Collections.emptyMap();
        }

        // Get all comment counts
        List<CommentCountProjection> commentCountProjections =
            baseCommentRepository.countByPostIds(postIds);

        return ServiceUtils.convertToMap(commentCountProjections, CommentCountProjection::getPostId,
            CommentCountProjection::getCount);
    }

    @Override
    public Map<Integer, Long> countByStatusAndPostIds(@NonNull CommentStatus status,
        @NonNull Collection<Integer> postIds) {
        if (CollectionUtils.isEmpty(postIds)) {
            return Collections.emptyMap();
        }

        // Get all comment counts
        List<CommentCountProjection> commentCountProjections =
            baseCommentRepository.countByStatusAndPostIds(status, postIds);

        return ServiceUtils.convertToMap(commentCountProjections, CommentCountProjection::getPostId,
            CommentCountProjection::getCount);
    }

    @Override
    public long countByPostId(@NonNull Integer postId) {
        Assert.notNull(postId, "Post id must not be null");
        return baseCommentRepository.countByPostId(postId);
    }

    @Override
    public long countByStatusAndPostId(@NonNull CommentStatus status, @NonNull Integer postId) {
        Assert.notNull(postId, "Post id must not be null");
        return baseCommentRepository.countByStatusAndPostId(status, postId);
    }

    @Override
    public long countByStatus(@NonNull CommentStatus status) {
        return baseCommentRepository.countByStatus(status);
    }

    @Override
    @NonNull
    @Transactional(rollbackFor = Exception.class)
    public COMMENT create(@NonNull COMMENT comment) {
        Assert.notNull(comment, "Domain must not be null");

        // Check post id
        if (!ServiceUtils.isEmptyId(comment.getPostId())) {
            validateTarget(comment.getPostId());
        }

        // Check parent id
        if (!ServiceUtils.isEmptyId(comment.getParentId())) {
            mustExistById(comment.getParentId());
        }

        // Check user login status and set this field
        final Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        // Set some default values
        if (comment.getIpAddress() == null) {
            comment.setIpAddress(ServletUtils.getRequestIp());
        }

        if (comment.getUserAgent() == null) {
            comment.setUserAgent(ServletUtils.getHeaderIgnoreCase(HttpHeaders.USER_AGENT));
        }

        if (comment.getGravatarMd5() == null) {
            comment.setGravatarMd5(
                DigestUtils.md5Hex(Optional.ofNullable(comment.getEmail()).orElse("")));
        }

        if (StringUtils.isNotEmpty(comment.getAuthorUrl())) {
            comment.setAuthorUrl(HaloUtils.normalizeUrl(comment.getAuthorUrl()));
        }

        if (authentication != null) {
            // Comment of blogger
            comment.setIsAdmin(true);
            comment.setStatus(CommentStatus.PUBLISHED);
        } else {
            // Comment of guest
            // Handle comment status
            Boolean needAudit = optionService
                .getByPropertyOrDefault(CommentProperties.NEW_NEED_CHECK, Boolean.class, true);
            comment.setStatus(needAudit ? CommentStatus.AUDITING : CommentStatus.PUBLISHED);
        }

        // Create comment
        COMMENT createdComment = super.create(comment);

        if (ServiceUtils.isEmptyId(createdComment.getParentId())) {
            if (authentication == null) {
                // New comment of guest
                eventPublisher.publishEvent(new CommentNewEvent(this, createdComment.getId()));
            }
        } else {
            // Reply comment
            eventPublisher.publishEvent(new CommentReplyEvent(this, createdComment.getId()));
        }

        return createdComment;
    }

    @Override
    @NonNull
    @Transactional(rollbackFor = Exception.class)
    public COMMENT createBy(@NonNull BaseCommentParam<COMMENT> commentParam) {
        Assert.notNull(commentParam, "Comment param must not be null");

        // Check user login status and set this field
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            // Blogger comment
            User user = authentication.getDetail().getUser();
            commentParam.setAuthor(
                StringUtils.isBlank(user.getNickname()) ? user.getUsername() : user.getNickname());
            commentParam.setEmail(user.getEmail());
            commentParam.setAuthorUrl(
                optionService.getByPropertyOrDefault(BlogProperties.BLOG_URL, String.class, null));
        }

        // Validate the comment param manually
        ValidationUtils.validate(commentParam);

        if (authentication == null) {
            // Anonymous comment
            // Check email
            if (userService.getByEmail(commentParam.getEmail()).isPresent()) {
                throw new BadRequestException("不能使用博主的邮箱，如果您是博主，请登录管理端进行回复。");
            }
        }

        // Convert to comment
        return create(commentParam.convertTo());
    }

    @Override
    @NonNull
    @Transactional(rollbackFor = Exception.class)
    public COMMENT updateStatus(@NonNull Long commentId, @NonNull CommentStatus status) {
        Assert.notNull(commentId, "Comment id must not be null");
        Assert.notNull(status, "Comment status must not be null");

        // Get comment by id
        COMMENT comment = getById(commentId);

        //Send mail if approved
        if (comment.getStatus().equals(CommentStatus.AUDITING)
            && status.equals(CommentStatus.PUBLISHED)) {
            if (!ServiceUtils.isEmptyId(comment.getParentId())) {
                eventPublisher.publishEvent(new CommentReplyEvent(this, comment.getId()));
            }
        }

        // Set comment status
        comment.setStatus(status);

        // Update comment
        return update(comment);
    }

    @Override
    @NonNull
    @Transactional(rollbackFor = Exception.class)
    public List<COMMENT> updateStatusByIds(@NonNull List<Long> ids, @NonNull CommentStatus status) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return ids.stream().map(id -> {
            return updateStatus(id, status);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<COMMENT> removeByPostId(@NonNull Integer postId) {
        Assert.notNull(postId, "Post id must not be null");
        return baseCommentRepository.deleteByPostId(postId);
    }

    @Override
    @NonNull
    @Transactional(rollbackFor = Exception.class)
    public COMMENT removeById(@NonNull Long id) {
        Assert.notNull(id, "Comment id must not be null");

        COMMENT comment = baseCommentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("查询不到该评论的信息").setErrorData(id));

        List<COMMENT> children =
            listChildrenBy(comment.getPostId(), id, Sort.by(DESC, "createTime"));

        if (children.size() > 0) {
            children.forEach(child -> {
                super.removeById(child.getId());
            });
        }

        return super.removeById(id);
    }

    @Override
    @NonNull
    @Transactional(rollbackFor = Exception.class)
    public List<COMMENT> removeByIds(@NonNull Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return ids.stream().map(this::removeById).collect(Collectors.toList());
    }

    @NonNull
    protected Specification<COMMENT> buildSpecByQuery(@NonNull CommentQuery commentQuery) {
        Assert.notNull(commentQuery, "Comment query must not be null");

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new LinkedList<>();

            if (commentQuery.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), commentQuery.getStatus()));
            }

            if (commentQuery.getKeyword() != null) {

                String likeCondition =
                    String.format("%%%s%%", StringUtils.strip(commentQuery.getKeyword()));

                Predicate authorLike = criteriaBuilder.like(root.get("author"), likeCondition);
                Predicate contentLike = criteriaBuilder.like(root.get("content"), likeCondition);
                Predicate emailLike = criteriaBuilder.like(root.get("email"), likeCondition);

                predicates.add(criteriaBuilder.or(authorLike, contentLike, emailLike));
            }

            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };
    }

    @Override
    @NonNull
    public Page<CommentWithHasChildrenVO> pageTopCommentsBy(@NonNull Integer targetId,
        @NonNull CommentStatus status,
        @NonNull Pageable pageable) {
        Assert.notNull(targetId, "Target id must not be null");
        Assert.notNull(status, "Comment status must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        // Get all comments
        Page<COMMENT> topCommentPage = baseCommentRepository
            .findAllByPostIdAndStatusAndParentId(targetId, status, 0L, pageable);

        if (topCommentPage.isEmpty()) {
            // If the comments is empty
            return ServiceUtils.buildEmptyPageImpl(topCommentPage);
        }

        // Get top comment ids
        Set<Long> topCommentIds =
            ServiceUtils.fetchProperty(topCommentPage.getContent(), BaseComment::getId);

        // Get direct children count
        List<CommentChildrenCountProjection> directChildrenCount =
            baseCommentRepository.findDirectChildrenCount(topCommentIds, CommentStatus.PUBLISHED);

        // Convert to comment - children count map
        Map<Long, Long> commentChildrenCountMap = ServiceUtils
            .convertToMap(directChildrenCount, CommentChildrenCountProjection::getCommentId,
                CommentChildrenCountProjection::getDirectChildrenCount);

        // Convert to comment with has children vo
        return topCommentPage.map(topComment -> {
            CommentWithHasChildrenVO comment =
                new CommentWithHasChildrenVO().convertFrom(topComment);
            comment
                .setHasChildren(commentChildrenCountMap.getOrDefault(topComment.getId(), 0L) > 0);
            comment.setAvatar(commentAssembler.buildAvatarUrl(topComment.getGravatarMd5()));
            return comment;
        });
    }

    @Override
    @NonNull
    public List<COMMENT> listChildrenBy(@NonNull Integer targetId, @NonNull Long commentParentId,
        @NonNull CommentStatus status, @NonNull Sort sort) {
        Assert.notNull(targetId, "Target id must not be null");
        Assert.notNull(commentParentId, "Comment parent id must not be null");
        Assert.notNull(sort, "Sort info must not be null");

        // Get comments recursively

        // Get direct children
        List<COMMENT> directChildren = baseCommentRepository
            .findAllByPostIdAndStatusAndParentId(targetId, status, commentParentId);

        // Create result container
        Set<COMMENT> children = new HashSet<>();

        // Get children comments
        getChildrenRecursively(directChildren, status, children);

        // Sort children
        List<COMMENT> childrenList = new ArrayList<>(children);
        childrenList.sort(Comparator.comparing(BaseComment::getId));

        return childrenList;
    }

    @Override
    @NonNull
    public List<COMMENT> listChildrenBy(@NonNull Integer targetId, @NonNull Long commentParentId,
        @NonNull Sort sort) {
        Assert.notNull(targetId, "Target id must not be null");
        Assert.notNull(commentParentId, "Comment parent id must not be null");
        Assert.notNull(sort, "Sort info must not be null");

        // Get comments recursively

        // Get direct children
        List<COMMENT> directChildren =
            baseCommentRepository.findAllByPostIdAndParentId(targetId, commentParentId);

        // Create result container
        Set<COMMENT> children = new HashSet<>();

        // Get children comments
        getChildrenRecursively(directChildren, children);

        // Sort children
        List<COMMENT> childrenList = new ArrayList<>(children);
        childrenList.sort(Comparator.comparing(BaseComment::getId));

        return childrenList;
    }

    /**
     * Get children comments recursively.
     *
     * @param topComments top comment list
     * @param status comment status must not be null
     * @param children children result must not be null
     */
    private void getChildrenRecursively(@Nullable List<COMMENT> topComments,
        @NonNull CommentStatus status, @NonNull Set<COMMENT> children) {
        Assert.notNull(status, "Comment status must not be null");
        Assert.notNull(children, "Children comment set must not be null");

        if (CollectionUtils.isEmpty(topComments)) {
            return;
        }

        // Convert comment id set
        Set<Long> commentIds = ServiceUtils.fetchProperty(topComments, COMMENT::getId);

        // Get direct children
        List<COMMENT> directChildren =
            baseCommentRepository.findAllByStatusAndParentIdIn(status, commentIds);

        // Recursively invoke
        getChildrenRecursively(directChildren, status, children);

        // Add direct children to children result
        children.addAll(topComments);
    }

    /**
     * Get children comments recursively.
     *
     * @param topComments top comment list
     * @param children children result must not be null
     */
    private void getChildrenRecursively(@Nullable List<COMMENT> topComments,
        @NonNull Set<COMMENT> children) {
        Assert.notNull(children, "Children comment set must not be null");

        if (CollectionUtils.isEmpty(topComments)) {
            return;
        }

        // Convert comment id set
        Set<Long> commentIds = ServiceUtils.fetchProperty(topComments, COMMENT::getId);

        // Get direct children
        List<COMMENT> directChildren = baseCommentRepository.findAllByParentIdIn(commentIds);

        // Recursively invoke
        getChildrenRecursively(directChildren, children);

        // Add direct children to children result
        children.addAll(topComments);
    }
}
