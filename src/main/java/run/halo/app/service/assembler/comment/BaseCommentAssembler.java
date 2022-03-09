package run.halo.app.service.assembler.comment;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.model.dto.BaseCommentDTO;
import run.halo.app.model.entity.BaseComment;
import run.halo.app.model.properties.CommentProperties;
import run.halo.app.model.support.CommentPage;
import run.halo.app.model.vo.BaseCommentVO;
import run.halo.app.model.vo.BaseCommentWithParentVO;
import run.halo.app.service.OptionService;

/**
 * Base comment assembler.
 *
 * @author guqing
 * @date 2022-03-08
 */
@Slf4j
public abstract class BaseCommentAssembler<COMMENT extends BaseComment> {

    private final OptionService optionService;

    public BaseCommentAssembler(OptionService optionService) {
        this.optionService = optionService;
    }

    /**
     * Converts to base comment dto.
     *
     * @param comment comment must not be null
     * @return base comment dto
     */
    @NonNull
    public BaseCommentDTO convertTo(@NonNull COMMENT comment) {
        Assert.notNull(comment, "Comment must not be null");

        BaseCommentDTO baseCommentDto = new BaseCommentDTO().convertFrom(comment);

        baseCommentDto.setAvatar(buildAvatarUrl(comment.getGravatarMd5()));

        return baseCommentDto;
    }

    /**
     * Converts to base comment dto list.
     *
     * @param comments comment list must not be null
     * @return a list of base comment dto
     */
    @NonNull
    public List<BaseCommentDTO> convertTo(@NonNull List<COMMENT> comments) {
        if (CollectionUtils.isEmpty(comments)) {
            return Collections.emptyList();
        }
        return comments.stream()
            .map(this::convertTo)
            .collect(Collectors.toList());
    }

    /**
     * Converts to base comment dto page.
     *
     * @param commentPage comment page must not be null
     * @return a page of base comment dto
     */
    @NonNull
    public Page<BaseCommentDTO> convertTo(@NonNull Page<COMMENT> commentPage) {
        Assert.notNull(commentPage, "Comment page must not be null");

        return commentPage.map(this::convertTo);
    }

    /**
     * Converts to base comment vo tree.
     *
     * @param comments comments list could be null
     * @param comparator comment comparator could be null
     * @return a comment vo tree
     */
    public List<BaseCommentVO> convertToVo(@Nullable List<COMMENT> comments,
        @Nullable Comparator<BaseCommentVO> comparator) {
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

    /**
     * Lists comment vos by list of COMMENT.
     *
     * @param comments comments must not be null
     * @param pageable page info must not be null
     * @return a page of comment vo
     */
    @NonNull
    public Page<BaseCommentVO> pageVosBy(@NonNull List<COMMENT> comments,
        @NonNull Pageable pageable) {
        Assert.notNull(comments, "Comments must not be null");
        Assert.notNull(pageable, "Page info must not be null");

        Comparator<BaseCommentVO> commentComparator =
            buildCommentComparator(pageable.getSortOr(Sort.by(Sort.Direction.DESC, "createTime")));

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

    /**
     * Concretes comment tree.
     *
     * @param parentComment parent comment vo must not be null
     * @param comments comment list must not null
     * @param commentComparator comment vo comparator
     */
    public void concreteTree(@NonNull BaseCommentVO parentComment,
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
            BaseCommentVO commentVo = new BaseCommentVO().convertFrom(comment);

            commentVo.setAvatar(buildAvatarUrl(commentVo.getGravatarMd5()));

            if (parentComment.getChildren() == null) {
                parentComment.setChildren(new LinkedList<>());
            }

            parentComment.getChildren().add(commentVo);
        });

        // Remove children
        comments.removeAll(children);

        if (!CollectionUtils.isEmpty(parentComment.getChildren())) {
            // Recursively concrete the children
            parentComment.getChildren()
                .forEach(childComment -> concreteTree(childComment, comments, commentComparator));
            // Sort the children
            if (commentComparator != null) {
                parentComment.getChildren().sort(commentComparator);
            }
        }
    }

    /**
     * Build avatar url by gravatarMd5.
     *
     * @param gravatarMd5 gravatarMd5
     * @return avatar url
     */
    public String buildAvatarUrl(String gravatarMd5) {
        final String gravatarSource =
            optionService.getByPropertyOrDefault(CommentProperties.GRAVATAR_SOURCE, String.class);
        final String gravatarDefault =
            optionService.getByPropertyOrDefault(CommentProperties.GRAVATAR_DEFAULT, String.class);

        return gravatarSource + gravatarMd5 + "?s=256&d=" + gravatarDefault;
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

    /**
     * clear sensitive field value for theme render.
     *
     * @param comment comment
     */
    public void clearSensitiveField(@Nullable COMMENT comment) {
        if (comment == null) {
            return;
        }
        comment.setIpAddress(null);
        comment.setEmail(null);
    }

    /**
     * clear sensitive field value for theme render.
     *
     * @param comment comment tree
     */
    public void clearSensitiveField(@Nullable BaseCommentDTO comment) {
        if (comment == null) {
            return;
        }
        comment.setIpAddress(null);
        comment.setEmail(null);
    }

    /**
     * clear sensitive field value for theme render.
     *
     * @param commentTree comment tree
     */
    public void clearSensitiveField(@Nullable BaseCommentVO commentTree) {
        if (commentTree == null) {
            return;
        }
        Queue<BaseCommentVO> queue = new ArrayDeque<>();
        queue.add(commentTree);
        while (!queue.isEmpty()) {
            BaseCommentVO comment = queue.poll();
            comment.setIpAddress(null);
            comment.setEmail(null);

            if (!CollectionUtils.isEmpty(comment.getChildren())) {
                queue.addAll(comment.getChildren());
            }
        }
    }

    /**
     * clear sensitive field value for theme render.
     *
     * @param comment comment
     */
    public void clearSensitiveField(@Nullable BaseCommentWithParentVO comment) {
        if (comment == null) {
            return;
        }
        BaseCommentWithParentVO parent = comment.getParent();
        if (parent != null) {
            parent.setIpAddress(null);
            parent.setEmail(null);
        }
        comment.setIpAddress(null);
        comment.setEmail(null);
    }
}
