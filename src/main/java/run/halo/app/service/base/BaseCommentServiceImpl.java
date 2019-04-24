package run.halo.app.service.base;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.event.comment.CommentNewEvent;
import run.halo.app.event.comment.CommentPassEvent;
import run.halo.app.event.comment.CommentReplyEvent;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.entity.BaseComment;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.params.CommentQuery;
import run.halo.app.model.projection.CommentCountProjection;
import run.halo.app.model.properties.CommentProperties;
import run.halo.app.model.support.CommentPage;
import run.halo.app.model.vo.BaseCommentVO;
import run.halo.app.model.vo.BaseCommentWithParentVO;
import run.halo.app.model.vo.CommentWithParentVO;
import run.halo.app.repository.PostRepository;
import run.halo.app.repository.base.BaseCommentRepository;
import run.halo.app.security.authentication.Authentication;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.service.OptionService;
import run.halo.app.utils.ServiceUtils;
import run.halo.app.utils.ServletUtils;

import javax.persistence.criteria.Predicate;
import java.util.*;

/**
 * Base comment service implementation.
 *
 * @author johnniang
 * @date 19-4-24
 */
@Slf4j
public abstract class BaseCommentServiceImpl<COMMENT extends BaseComment> extends AbstractCrudService<COMMENT, Long> implements BaseCommentService<COMMENT> {

    private final BaseCommentRepository<COMMENT> baseCommentRepository;

    protected final PostRepository postRepository;

    protected final OptionService optionService;

    protected final ApplicationEventPublisher eventPublisher;

    public BaseCommentServiceImpl(BaseCommentRepository<COMMENT> baseCommentRepository,
                                  PostRepository postRepository,
                                  OptionService optionService,
                                  ApplicationEventPublisher eventPublisher) {
        super(baseCommentRepository);
        this.baseCommentRepository = baseCommentRepository;
        this.postRepository = postRepository;
        this.optionService = optionService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<COMMENT> listBy(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        return baseCommentRepository.findAllByPostId(postId);
    }

    @Override
    public Page<COMMENT> pageLatest(int top) {
        Assert.isTrue(top > 0, "Top number must not be less than 0");

        // Build page request
        PageRequest latestPageable = PageRequest.of(0, top, Sort.by(Sort.Direction.DESC, "createTime"));

        return listAll(latestPageable);
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
    public Page<BaseCommentVO> pageVosBy(Integer postId, Pageable pageable) {
        Assert.notNull(postId, "Post id must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        log.debug("Getting comment tree view of post: [{}], page info: [{}]", postId, pageable);

        // List all the top comments (Caution: This list will be cleared)
        List<COMMENT> comments = baseCommentRepository.findAllByPostIdAndStatus(postId, CommentStatus.PUBLISHED);

        // Init the top virtual comment
        BaseCommentVO topVirtualComment = new BaseCommentVO();
        topVirtualComment.setId(0L);
        topVirtualComment.setChildren(new LinkedList<>());

        Comparator<BaseCommentVO> commentVOComparator = buildCommentComparator(pageable.getSortOr(Sort.by(Sort.Direction.DESC, "createTime")));

        // Concrete the comment tree
        concreteTree(topVirtualComment, new LinkedList<>(comments), commentVOComparator);

        List<BaseCommentVO> topComments = topVirtualComment.getChildren();

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
                    parentCommentVo = new CommentWithParentVO().convertFrom(parentComment);
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
    public COMMENT createBy(COMMENT COMMENT) {
        Assert.notNull(COMMENT, "Domain must not be null");

        // Check post id
        boolean isPostExist = postRepository.existsById(COMMENT.getPostId());
        if (!isPostExist) {
            throw new NotFoundException("The post with id " + COMMENT.getPostId() + " was not found");
        }

        // Check parent id
        if (!ServiceUtils.isEmptyId(COMMENT.getParentId())) {
            mustExistById(COMMENT.getParentId());
        }

        // Check user login status and set this field
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Set some default values
        COMMENT.setIpAddress(ServletUtils.getRequestIp());
        COMMENT.setUserAgent(ServletUtils.getHeaderIgnoreCase(HttpHeaders.USER_AGENT));
        COMMENT.setGavatarMd5(DigestUtils.md5Hex(COMMENT.getEmail()));

        if (authentication != null) {
            // Comment of blogger
            COMMENT.setIsAdmin(true);
            COMMENT.setStatus(CommentStatus.PUBLISHED);
        } else {
            // Comment of guest
            // Handle comment status
            Boolean needAudit = optionService.getByPropertyOrDefault(CommentProperties.NEW_NEED_CHECK, Boolean.class, true);
            COMMENT.setStatus(needAudit ? CommentStatus.AUDITING : CommentStatus.PUBLISHED);
        }

        // Create comment
        COMMENT createdComment = create(COMMENT);

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
    public COMMENT updateStatus(Long commentId, CommentStatus status) {
        Assert.notNull(commentId, "Comment id must not be null");
        Assert.notNull(status, "Comment status must not be null");

        // Get comment by id
        COMMENT comment = getById(commentId);

        // Set comment status
        comment.setStatus(status);

        // Update comment
        COMMENT updatedComment = update(comment);

        if (CommentStatus.PUBLISHED.equals(status)) {
            // Pass a comment
            eventPublisher.publishEvent(new CommentPassEvent(this, commentId));
        }

        return updatedComment;
    }

    @NonNull
    private Specification<COMMENT> buildSpecByQuery(@NonNull CommentQuery commentQuery) {
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
    private Comparator<BaseCommentVO> buildCommentComparator(Sort sort) {
        return (currentComment, toCompareComment) -> {
            Assert.notNull(currentComment, "Current comment must not be null");
            Assert.notNull(toCompareComment, "Comment to compare must not be null");

            // Get sort order
            Sort.Order order = sort.filter(anOrder -> anOrder.getProperty().equals("createTime"))
                    .get()
                    .findFirst()
                    .orElseGet(() -> Sort.Order.desc("createTime"));

            // Init sign
            int sign = order.getDirection().isAscending() ? 1 : -1;

            // Compare createTime property
            return sign * currentComment.getCreateTime().compareTo(toCompareComment.getCreateTime());
        };
    }

    /**
     * Concretes comment tree.
     *
     * @param parentComment     parent comment vo must not be null
     * @param comments          comment list must not null
     * @param commentComparator comment vo comparator
     */
    private void concreteTree(@NonNull BaseCommentVO parentComment,
                              @Nullable Collection<COMMENT> comments,
                              @NonNull Comparator<BaseCommentVO> commentComparator) {
        Assert.notNull(parentComment, "Parent comment must not be null");
        Assert.notNull(commentComparator, "Comment comparator must not be null");

        if (CollectionUtils.isEmpty(comments)) {
            return;
        }

        List<COMMENT> children = new LinkedList<>();

        comments.forEach(comment -> {
            if (parentComment.getId().equals(comment.getParentId())) {
                // Stage the child comment
                children.add(comment);

                // Convert to comment vo
                BaseCommentVO commentVO = new BaseCommentVO().convertFrom(comment);

                // Add additional content
                if (commentVO.getParentId() > 0) {
                    // TODO Provide an optional additional content
                    commentVO.setContent(String.format(COMMENT_TEMPLATE, parentComment.getId(), parentComment.getAuthor(), commentVO.getContent()));
                }

                // Init children container
                if (parentComment.getChildren() == null) {
                    parentComment.setChildren(new LinkedList<>());
                }

                parentComment.getChildren().add(commentVO);
            }
        });

        // Remove all children
        comments.removeAll(children);

        if (!CollectionUtils.isEmpty(parentComment.getChildren())) {
            // Recursively concrete the children
            parentComment.getChildren().forEach(childComment -> concreteTree(childComment, comments, commentComparator));
            // Sort the children
            parentComment.getChildren().sort(commentComparator);
        }
    }
}
