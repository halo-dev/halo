package run.halo.app.service.impl;

import cn.hutool.core.util.URLUtil;
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
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.event.comment.CommentNewEvent;
import run.halo.app.event.comment.CommentReplyEvent;
import run.halo.app.exception.BadRequestException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.entity.BaseComment;
import run.halo.app.model.entity.User;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.params.BaseCommentParam;
import run.halo.app.model.params.CommentQuery;
import run.halo.app.model.projection.CommentChildrenCountProjection;
import run.halo.app.model.projection.CommentCountProjection;
import run.halo.app.model.properties.BlogProperties;
import run.halo.app.model.properties.CommentProperties;
import run.halo.app.model.support.CommentPage;
import run.halo.app.model.vo.BaseCommentVO;
import run.halo.app.model.vo.BaseCommentWithParentVO;
import run.halo.app.model.vo.CommentWithHasChildrenVO;
import run.halo.app.repository.base.BaseCommentRepository;
import run.halo.app.security.authentication.Authentication;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.service.OptionService;
import run.halo.app.service.UserService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.service.base.BaseCommentService;
import run.halo.app.utils.ServiceUtils;
import run.halo.app.utils.ServletUtils;
import run.halo.app.utils.ValidationUtils;

import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Base comment service implementation.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-24
 */
@Slf4j
public abstract class BaseCommentServiceImpl<COMMENT extends BaseComment> extends AbstractCrudService<COMMENT, Long> implements BaseCommentService<COMMENT> {

    protected final OptionService optionService;
    protected final UserService userService;
    protected final ApplicationEventPublisher eventPublisher;
    private final BaseCommentRepository<COMMENT> baseCommentRepository;

    public BaseCommentServiceImpl(BaseCommentRepository<COMMENT> baseCommentRepository,
                                  OptionService optionService,
                                  UserService userService, ApplicationEventPublisher eventPublisher) {
        super(baseCommentRepository);
        this.baseCommentRepository = baseCommentRepository;
        this.optionService = optionService;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<COMMENT> listBy(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        return baseCommentRepository.findAllByPostId(postId);
    }

    @Override
    public Page<COMMENT> pageLatest(int top) {
        return pageLatest(top, null);
    }

    @Override
    public Page<COMMENT> pageLatest(int top, CommentStatus status) {
        if (status == null) {
            return listAll(ServiceUtils.buildLatestPageable(top));
        }

        return baseCommentRepository.findAllByStatus(status, ServiceUtils.buildLatestPageable(top));
    }

    @Override
    public Page<COMMENT> pageBy(CommentStatus status, Pageable pageable) {

        Assert.notNull(status, "Comment status must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        // Find all
        return baseCommentRepository.findAllByStatus(status, pageable);
    }

    @Override
    public Page<COMMENT> pageBy(CommentQuery commentQuery, Pageable pageable) {
        Assert.notNull(pageable, "Page info must not be null");

        return baseCommentRepository.findAll(buildSpecByQuery(commentQuery), pageable);
    }

    @Override
    public Page<BaseCommentVO> pageVosAllBy(Integer postId, Pageable pageable) {
        Assert.notNull(postId, "Post id must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        log.debug("Getting comment tree view of post: [{}], page info: [{}]", postId, pageable);

        // List all the top comments (Caution: This list will be cleared)
        List<COMMENT> comments = baseCommentRepository.findAllByPostId(postId);

        return pageVosBy(comments, pageable);
    }

    @Override
    public Page<BaseCommentVO> pageVosBy(List<COMMENT> comments, Pageable pageable) {
        Assert.notNull(comments, "Comments must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        Comparator<BaseCommentVO> commentComparator = buildCommentComparator(pageable.getSortOr(Sort.by(Sort.Direction.DESC, "createTime")));

        // Convert to vo
        List<BaseCommentVO> topComments = convertToVo(comments, commentComparator);

        List<BaseCommentVO> pageContent;

        // Calc the shear index
        int startIndex = pageable.getPageNumber() * pageable.getPageSize();
        if (startIndex >= topComments.size() || startIndex < 0) {
            pageContent = Collections.emptyList();
        } else {
            int endIndex = startIndex + pageable.getPageSize();
            if (endIndex > topComments.size()) {
                endIndex = topComments.size();
            }

            log.debug("Top comments size: [{}]", topComments.size());
            log.debug("Start index: [{}]", startIndex);
            log.debug("End index: [{}]", endIndex);

            pageContent = topComments.subList(startIndex, endIndex);
        }

        return new CommentPage<>(pageContent, pageable, topComments.size(), comments.size());
    }

    @Override
    public Page<BaseCommentVO> pageVosBy(Integer postId, Pageable pageable) {
        Assert.notNull(postId, "Post id must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        log.debug("Getting comment tree view of post: [{}], page info: [{}]", postId, pageable);

        // List all the top comments (Caution: This list will be cleared)
        List<COMMENT> comments = baseCommentRepository.findAllByPostIdAndStatus(postId, CommentStatus.PUBLISHED);

        return pageVosBy(comments, pageable);
    }

    @Override
    public Page<BaseCommentWithParentVO> pageWithParentVoBy(Integer postId, Pageable pageable) {
        Assert.notNull(postId, "Post id must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        log.debug("Getting comment list view of post: [{}], page info: [{}]", postId, pageable);

        // List all the top comments (Caution: This list will be cleared)
        Page<COMMENT> commentPage = baseCommentRepository.findAllByPostIdAndStatus(postId, CommentStatus.PUBLISHED, pageable);

        // Get all comments
        List<COMMENT> comments = commentPage.getContent();

        // Get all comment parent ids
        Set<Long> parentIds = ServiceUtils.fetchProperty(comments, COMMENT::getParentId);

        // Get all parent comments
        List<COMMENT> parentComments = baseCommentRepository.findAllByIdIn(parentIds, pageable.getSort());

        // Convert to comment map (Key: comment id, value: comment)
        Map<Long, COMMENT> parentCommentMap = ServiceUtils.convertToMap(parentComments, COMMENT::getId);

        Map<Long, BaseCommentWithParentVO> parentCommentVoMap = new HashMap<>(parentCommentMap.size());

        // Convert to comment page
        return commentPage.map(comment -> {
            // Convert to with parent vo
            BaseCommentWithParentVO commentWithParentVO = new BaseCommentWithParentVO().convertFrom(comment);

            // Get parent comment vo from cache
            BaseCommentWithParentVO parentCommentVo = parentCommentVoMap.get(comment.getParentId());

            if (parentCommentVo == null) {
                // Get parent comment
                COMMENT parentComment = parentCommentMap.get(comment.getParentId());

                if (parentComment != null) {
                    // Convert to parent comment vo
                    parentCommentVo = new BaseCommentWithParentVO().convertFrom(parentComment);
                    // Cache the parent comment vo
                    parentCommentVoMap.put(parentComment.getId(), parentCommentVo);
                }
            }

            // Set parent
            commentWithParentVO.setParent(parentCommentVo == null ? null : parentCommentVo.clone());

            return commentWithParentVO;
        });
    }

    @Override
    public Map<Integer, Long> countByPostIds(Collection<Integer> postIds) {
        if (CollectionUtils.isEmpty(postIds)) {
            return Collections.emptyMap();
        }

        // Get all comment counts
        List<CommentCountProjection> commentCountProjections = baseCommentRepository.countByPostIds(postIds);

        return ServiceUtils.convertToMap(commentCountProjections, CommentCountProjection::getPostId, CommentCountProjection::getCount);
    }

    @Override
    public long countByPostId(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");
        return baseCommentRepository.countByPostId(postId);
    }

    @Override
    public long countByStatus(CommentStatus status) {
        return baseCommentRepository.countByStatus(status);
    }

    @Override
    public COMMENT create(COMMENT comment) {
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Set some default values
        if (comment.getIpAddress() == null) {
            comment.setIpAddress(ServletUtils.getRequestIp());
        }

        if (comment.getUserAgent() == null) {
            comment.setUserAgent(ServletUtils.getHeaderIgnoreCase(HttpHeaders.USER_AGENT));
        }

        if (comment.getGravatarMd5() == null) {
            comment.setGravatarMd5(DigestUtils.md5Hex(comment.getEmail()));
        }

        if (StringUtils.isNotEmpty(comment.getAuthorUrl())) {
            comment.setAuthorUrl(URLUtil.normalize(comment.getAuthorUrl()));
        }

        if (authentication != null) {
            // Comment of blogger
            comment.setIsAdmin(true);
            comment.setStatus(CommentStatus.PUBLISHED);
        } else {
            // Comment of guest
            // Handle comment status
            Boolean needAudit = optionService.getByPropertyOrDefault(CommentProperties.NEW_NEED_CHECK, Boolean.class, true);
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
    public COMMENT createBy(BaseCommentParam<COMMENT> commentParam) {
        Assert.notNull(commentParam, "Comment param must not be null");

        // Check user login status and set this field
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            // Blogger comment
            User user = authentication.getDetail().getUser();
            commentParam.setAuthor(StringUtils.isBlank(user.getNickname()) ? user.getUsername() : user.getNickname());
            commentParam.setEmail(user.getEmail());
            commentParam.setAuthorUrl(optionService.getByPropertyOrDefault(BlogProperties.BLOG_URL, String.class, null));
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
    public COMMENT updateStatus(Long commentId, CommentStatus status) {
        Assert.notNull(commentId, "Comment id must not be null");
        Assert.notNull(status, "Comment status must not be null");

        // Get comment by id
        COMMENT comment = getById(commentId);

        // Set comment status
        comment.setStatus(status);

        // Update comment
        return update(comment);
    }

    @Override
    public List<COMMENT> updateStatusByIds(List<Long> ids, CommentStatus status) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return ids.stream().map(id -> {
            return updateStatus(id, status);
        }).collect(Collectors.toList());
    }

    @Override
    public List<COMMENT> removeByPostId(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");
        return baseCommentRepository.deleteByPostId(postId);
    }

    @Override
    public COMMENT removeById(Long id) {
        Assert.notNull(id, "Comment id must not be null");

        COMMENT comment = baseCommentRepository.findById(id).orElseThrow(() -> new NotFoundException("查询不到该评论的信息").setErrorData(id));

        List<COMMENT> children = listChildrenBy(comment.getPostId(), id, Sort.by(DESC, "createTime"));

        if (children.size() > 0) {
            children.forEach(child -> {
                super.removeById(child.getId());
            });
        }

        return super.removeById(id);
    }

    @Override
    public List<COMMENT> removeByIds(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return ids.stream().map(this::removeById).collect(Collectors.toList());
    }

    @Override
    public List<BaseCommentDTO> convertTo(List<COMMENT> comments) {
        if (CollectionUtils.isEmpty(comments)) {
            return Collections.emptyList();
        }
        return comments.stream()
            .map(this::convertTo)
            .collect(Collectors.toList());
    }

    @Override
    public Page<BaseCommentDTO> convertTo(Page<COMMENT> commentPage) {
        Assert.notNull(commentPage, "Comment page must not be null");

        return commentPage.map(this::convertTo);
    }

    @Override
    public BaseCommentDTO convertTo(COMMENT comment) {
        Assert.notNull(comment, "Comment must not be null");

        return new BaseCommentDTO().convertFrom(comment);
    }

    @NonNull
    protected Specification<COMMENT> buildSpecByQuery(@NonNull CommentQuery commentQuery) {
        Assert.notNull(commentQuery, "Comment query must not be null");

        return (Specification<COMMENT>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new LinkedList<>();

            if (commentQuery.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), commentQuery.getStatus()));
            }

            if (commentQuery.getKeyword() != null) {

                String likeCondition = String.format("%%%s%%", StringUtils.strip(commentQuery.getKeyword()));

                Predicate authorLike = criteriaBuilder.like(root.get("author"), likeCondition);
                Predicate contentLike = criteriaBuilder.like(root.get("content"), likeCondition);
                Predicate emailLike = criteriaBuilder.like(root.get("email"), likeCondition);

                predicates.add(criteriaBuilder.or(authorLike, contentLike, emailLike));
            }

            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };
    }

    /**
     * Builds a comment comparator.
     *
     * @param sort sort info
     * @return comment comparator
     */
    protected Comparator<BaseCommentVO> buildCommentComparator(Sort sort) {
        return (currentComment, toCompareComment) -> {
            Assert.notNull(currentComment, "Current comment must not be null");
            Assert.notNull(toCompareComment, "Comment to compare must not be null");

            // Get sort order
            Sort.Order order = sort.filter(anOrder -> "id".equals(anOrder.getProperty()))
                .get()
                .findFirst()
                .orElseGet(() -> Sort.Order.desc("id"));

            // Init sign
            int sign = order.getDirection().isAscending() ? 1 : -1;

            // Compare id property
            return sign * currentComment.getId().compareTo(toCompareComment.getId());
        };
    }

    @NonNull
    @Override
    public List<BaseCommentVO> convertToVo(@Nullable List<COMMENT> comments, @Nullable Comparator<BaseCommentVO> comparator) {
        if (CollectionUtils.isEmpty(comments)) {
            return Collections.emptyList();
        }

        // Init the top virtual comment
        BaseCommentVO topVirtualComment = new BaseCommentVO();
        topVirtualComment.setId(0L);
        topVirtualComment.setChildren(new LinkedList<>());

        // Concrete the comment tree
        concreteTree(topVirtualComment, new LinkedList<>(comments), comparator);

        return topVirtualComment.getChildren();
    }

    @Override
    public Page<CommentWithHasChildrenVO> pageTopCommentsBy(Integer targetId, CommentStatus status, Pageable pageable) {
        Assert.notNull(targetId, "Target id must not be null");
        Assert.notNull(status, "Comment status must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        // Get all comments
        Page<COMMENT> topCommentPage = baseCommentRepository.findAllByPostIdAndStatusAndParentId(targetId, status, 0L, pageable);

        if (topCommentPage.isEmpty()) {
            // If the comments is empty
            return ServiceUtils.buildEmptyPageImpl(topCommentPage);
        }

        // Get top comment ids
        Set<Long> topCommentIds = ServiceUtils.fetchProperty(topCommentPage.getContent(), BaseComment::getId);

        // Get direct children count
        List<CommentChildrenCountProjection> directChildrenCount = baseCommentRepository.findDirectChildrenCount(topCommentIds);

        // Convert to comment - children count map
        Map<Long, Long> commentChildrenCountMap = ServiceUtils.convertToMap(directChildrenCount, CommentChildrenCountProjection::getCommentId, CommentChildrenCountProjection::getDirectChildrenCount);

        // Convert to comment with has children vo
        return topCommentPage.map(topComment -> {
            CommentWithHasChildrenVO comment = new CommentWithHasChildrenVO().convertFrom(topComment);
            comment.setHasChildren(commentChildrenCountMap.getOrDefault(topComment.getId(), 0L) > 0);
            return comment;
        });
    }

    @Override
    public List<COMMENT> listChildrenBy(Integer targetId, Long commentParentId, CommentStatus status, Sort sort) {
        Assert.notNull(targetId, "Target id must not be null");
        Assert.notNull(commentParentId, "Comment parent id must not be null");
        Assert.notNull(sort, "Sort info must not be null");

        // Get comments recursively

        // Get direct children
        List<COMMENT> directChildren = baseCommentRepository.findAllByPostIdAndStatusAndParentId(targetId, status, commentParentId);

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
    public List<COMMENT> listChildrenBy(Integer targetId, Long commentParentId, Sort sort) {
        Assert.notNull(targetId, "Target id must not be null");
        Assert.notNull(commentParentId, "Comment parent id must not be null");
        Assert.notNull(sort, "Sort info must not be null");

        // Get comments recursively

        // Get direct children
        List<COMMENT> directChildren = baseCommentRepository.findAllByPostIdAndParentId(targetId, commentParentId);

        // Create result container
        Set<COMMENT> children = new HashSet<>();

        // Get children comments
        getChildrenRecursively(directChildren, children);

        // Sort children
        List<COMMENT> childrenList = new ArrayList<>(children);
        childrenList.sort(Comparator.comparing(BaseComment::getId));

        return childrenList;
    }

    @Override
    @Deprecated
    public <T extends BaseCommentDTO> T filterIpAddress(@NonNull T comment) {
        Assert.notNull(comment, "Base comment dto must not be null");

        // Clear ip address
        comment.setIpAddress("");

        // Handle base comment vo
        if (comment instanceof BaseCommentVO) {
            BaseCommentVO baseCommentVO = (BaseCommentVO) comment;
            Queue<BaseCommentVO> commentQueue = new LinkedList<>();
            commentQueue.offer(baseCommentVO);
            while (!commentQueue.isEmpty()) {
                BaseCommentVO current = commentQueue.poll();

                // Clear ip address
                current.setIpAddress("");

                if (!CollectionUtils.isEmpty(current.getChildren())) {
                    // Add children
                    commentQueue.addAll(current.getChildren());
                }
            }
        }

        return comment;
    }

    @Override
    @Deprecated
    public <T extends BaseCommentDTO> List<T> filterIpAddress(List<T> comments) {
        if (CollectionUtils.isEmpty(comments)) {
            return Collections.emptyList();
        }

        comments.forEach(this::filterIpAddress);

        return comments;
    }

    @Override
    @Deprecated
    public <T extends BaseCommentDTO> Page<T> filterIpAddress(Page<T> commentPage) {
        Assert.notNull(commentPage, "Comment page must not be null");
        commentPage.forEach(this::filterIpAddress);

        return commentPage;
    }

    @Override
    public List<BaseCommentDTO> replaceUrl(String oldUrl, String newUrl) {
        List<COMMENT> comments = listAll();
        List<COMMENT> replaced = new ArrayList<>();
        comments.forEach(comment -> {
            if (StringUtils.isNotEmpty(comment.getAuthorUrl())) {
                comment.setAuthorUrl(comment.getAuthorUrl().replaceAll(oldUrl, newUrl));
            }
            replaced.add(comment);
        });
        List<COMMENT> updated = updateInBatch(replaced);
        return convertTo(updated);
    }

    /**
     * Get children comments recursively.
     *
     * @param topComments top comment list
     * @param status      comment status must not be null
     * @param children    children result must not be null
     */
    private void getChildrenRecursively(@Nullable List<COMMENT> topComments, @NonNull CommentStatus status, @NonNull Set<COMMENT> children) {
        Assert.notNull(status, "Comment status must not be null");
        Assert.notNull(children, "Children comment set must not be null");

        if (CollectionUtils.isEmpty(topComments)) {
            return;
        }

        // Convert comment id set
        Set<Long> commentIds = ServiceUtils.fetchProperty(topComments, COMMENT::getId);

        // Get direct children
        List<COMMENT> directChildren = baseCommentRepository.findAllByStatusAndParentIdIn(status, commentIds);

        // Recursively invoke
        getChildrenRecursively(directChildren, status, children);

        // Add direct children to children result
        children.addAll(topComments);
    }

    /**
     * Get children comments recursively.
     *
     * @param topComments top comment list
     * @param children    children result must not be null
     */
    private void getChildrenRecursively(@Nullable List<COMMENT> topComments, @NonNull Set<COMMENT> children) {
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

    /**
     * Concretes comment tree.
     *
     * @param parentComment     parent comment vo must not be null
     * @param comments          comment list must not null
     * @param commentComparator comment vo comparator
     */
    protected void concreteTree(@NonNull BaseCommentVO parentComment,
                                @Nullable Collection<COMMENT> comments,
                                @Nullable Comparator<BaseCommentVO> commentComparator) {
        Assert.notNull(parentComment, "Parent comment must not be null");

        if (CollectionUtils.isEmpty(comments)) {
            return;
        }

        // Get children
        List<COMMENT> children = comments.stream()
            .filter(comment -> Objects.equals(parentComment.getId(), comment.getParentId()))
            .collect(Collectors.toList());

        // Add children
        children.forEach(comment -> {
            // Convert to comment vo
            BaseCommentVO commentVO = new BaseCommentVO().convertFrom(comment);

            if (parentComment.getChildren() == null) {
                parentComment.setChildren(new LinkedList<>());
            }

            parentComment.getChildren().add(commentVO);
        });

        // Remove children
        comments.removeAll(children);

        if (!CollectionUtils.isEmpty(parentComment.getChildren())) {
            // Recursively concrete the children
            parentComment.getChildren().forEach(childComment -> concreteTree(childComment, comments, commentComparator));
            // Sort the children
            if (commentComparator != null) {
                parentComment.getChildren().sort(commentComparator);
            }
        }
    }

}
