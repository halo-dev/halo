package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.dto.post.PostMinimalOutputDTO;
import cc.ryanc.halo.model.entity.Comment;
import cc.ryanc.halo.model.entity.Post;
import cc.ryanc.halo.model.enums.BlogProperties;
import cc.ryanc.halo.model.enums.CommentStatus;
import cc.ryanc.halo.model.projection.CommentCountProjection;
import cc.ryanc.halo.model.support.CommentPage;
import cc.ryanc.halo.model.vo.CommentListVO;
import cc.ryanc.halo.model.vo.CommentVO;
import cc.ryanc.halo.repository.CommentRepository;
import cc.ryanc.halo.repository.PostRepository;
import cc.ryanc.halo.service.CommentService;
import cc.ryanc.halo.service.OptionService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import cc.ryanc.halo.utils.OwoUtil;
import cc.ryanc.halo.utils.ServiceUtils;
import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.extra.servlet.ServletUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CommentService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Slf4j
@Service
public class CommentServiceImpl extends AbstractCrudService<Comment, Long> implements CommentService {

    private final CommentRepository commentRepository;

    private final PostRepository postRepository;

    private final OptionService optionService;

    public CommentServiceImpl(CommentRepository commentRepository,
                              PostRepository postRepository,
                              OptionService optionService) {
        super(commentRepository);
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.optionService = optionService;
    }

    @Override
    public Page<CommentListVO> pageLatest(int top) {
        Assert.isTrue(top > 0, "Top number must not be less than 0");

        // Build page request
        PageRequest latestPageable = PageRequest.of(0, top, Sort.by(Sort.Direction.DESC, "createTime"));

        return convertBy(listAll(latestPageable));
    }

    @Override
    public Page<CommentListVO> pageBy(CommentStatus status, Pageable pageable) {
        Assert.notNull(status, "Comment status must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        // Find all 
        Page<Comment> commentPage = commentRepository.findAllByStatus(status, pageable);

        return convertBy(commentPage);
    }

    @Override
    public List<Comment> listBy(Integer postId) {
        Assert.notNull(postId, "Post id must not be null");

        return commentRepository.findAllByPostId(postId);
    }

    @Override
    public Map<Integer, Long> countByPostIds(Collection<Integer> postIds) {
        if (CollectionUtils.isEmpty(postIds)) {
            return Collections.emptyMap();
        }

        // Get all comment counts
        List<CommentCountProjection> commentCountProjections = commentRepository.countByPostIds(postIds);

        return ServiceUtils.convertToMap(commentCountProjections, CommentCountProjection::getPostId, CommentCountProjection::getCount);
    }

    @Override
    public Comment createBy(Comment comment, HttpServletRequest request) {
        Assert.notNull(comment, "Comment must not be null");
        Assert.notNull(request, "Http servlet request must not be null");

        // Set some default value
        comment.setContent(OwoUtil.parseOwo(formatContent(comment.getContent())));
        comment.setIpAddress(ServletUtil.getClientIP(request));
        comment.setUserAgent(ServletUtil.getHeaderIgnoreCase(request, HttpHeaders.USER_AGENT));
        // TODO Check user login status and set this field
        comment.setIsAdmin(false);
        comment.setAuthor(HtmlUtils.htmlEscape(comment.getAuthor()));
        comment.setGavatarMd5(SecureUtil.md5(comment.getEmail()));

        if (StringUtils.isNotBlank(comment.getAuthorUrl())) {
            // Normalize the author url and set it
            comment.setAuthorUrl(URLUtil.normalize(comment.getAuthorUrl()));
        }

        // Handle comment status
        Boolean needAudit = optionService.getByProperty(BlogProperties.NEW_COMMENT_NEED_CHECK, Boolean.class, true);
        if (needAudit) {
            comment.setStatus(CommentStatus.AUDITING);
        } else {
            comment.setStatus(CommentStatus.PUBLISHED);
        }

        Comment createdComment = create(comment);

        // TODO Handle email sending

        return createdComment;
    }

    @Override
    public Page<CommentVO> pageVosBy(Integer postId, Pageable pageable) {
        Assert.notNull(postId, "Post id must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        log.debug("Getting comment of post: [{}], page info: [{}]", postId, pageable);

        // List all the top comments (Caution: This list will be cleared)
        List<Comment> comments = commentRepository.findAllByPostIdAndStatus(postId, CommentStatus.PUBLISHED);

        // Init the top virtual comment
        CommentVO topVirtualComment = new CommentVO();
        topVirtualComment.setId(0L);
        topVirtualComment.setChildren(new LinkedList<>());

        // Concrete the comment tree
        concreteTree(topVirtualComment, new LinkedList<>(comments), buildCommentComparator(pageable.getSortOr(Sort.by(Sort.Direction.DESC, "createTime"))));

        List<CommentVO> topComments = topVirtualComment.getChildren();

        List<CommentVO> pageContent = null;

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

    /**
     * Builds a comment comparator.
     *
     * @param sort sort info
     * @return comment comparator
     */
    private Comparator<CommentVO> buildCommentComparator(Sort sort) {
        return (currentComment, toCompareComment) -> {
            Assert.notNull(currentComment, "Current comment must not be null");
            Assert.notNull(toCompareComment, "Comment to compare must not be null");

            // Get sort order
            Order order = sort.filter(anOrder -> anOrder.getProperty().equals("createTime"))
                    .get()
                    .findFirst()
                    .orElseGet(() -> Order.desc("createTime"));

            // Init sign
            int sign = order.getDirection().isAscending() ? 1 : -1;

            // Compare createTime property
            return sign * currentComment.getCreateTime().compareTo(toCompareComment.getCreateTime());
        };
    }

    private void concreteTree(CommentVO parentComment, List<Comment> comments, Comparator<CommentVO> commentComparator) {
        Assert.notNull(parentComment, "Parent comment must not be null");
        Assert.notNull(commentComparator, "Comment comparator must not be null");

        if (CollectionUtils.isEmpty(comments)) {
            return;
        }

        List<Comment> children = new LinkedList<>();

        comments.forEach(comment -> {
            if (parentComment.getId().equals(comment.getParentId())) {
                // Stage the child comment
                children.add(comment);

                // Convert to comment vo
                CommentVO commentVO = new CommentVO().convertFrom(comment);

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

    /**
     * Converts to comment vo page.
     *
     * @param commentPage comment page must not be null
     * @return a page of comment vo
     */
    @NonNull
    private Page<CommentListVO> convertBy(@NonNull Page<Comment> commentPage) {
        Assert.notNull(commentPage, "Comment page must not be null");

        return new PageImpl<>(convertBy(commentPage.getContent()), commentPage.getPageable(), commentPage.getTotalElements());
    }

    /**
     * Converts to comment vo list.
     *
     * @param comments comment list
     * @return a list of comment vo
     */
    @NonNull
    private List<CommentListVO> convertBy(@Nullable List<Comment> comments) {
        if (CollectionUtils.isEmpty(comments)) {
            return Collections.emptyList();
        }

        // Fetch goods ids
        Set<Integer> postIds = ServiceUtils.fetchProperty(comments, Comment::getPostId);

        // Get all posts
        Map<Integer, Post> postMap = ServiceUtils.convertToMap(postRepository.findAllById(postIds), Post::getId);

        return comments.stream().map(comment -> {
            // Convert to vo
            CommentListVO commentListVO = new CommentListVO().convertFrom(comment);

            // Get post and set to the vo
            commentListVO.setPost(new PostMinimalOutputDTO().convertFrom(postMap.get(comment.getPostId())));

            return commentListVO;
        }).collect(Collectors.toList());
    }

    private String formatContent(@NonNull String content) {
        return HtmlUtils.htmlEscape(content).replace("&lt;br/&gt;", "<br/>");
    }

}
