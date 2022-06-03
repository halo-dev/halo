package run.halo.app.service.base;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.model.entity.BaseComment;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.params.BaseCommentParam;
import run.halo.app.model.params.CommentQuery;
import run.halo.app.model.vo.BaseCommentVO;
import run.halo.app.model.vo.BaseCommentWithParentVO;
import run.halo.app.model.vo.CommentWithHasChildrenVO;

/**
 * Base comment service interface.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-24
 */
public interface BaseCommentService<COMMENT extends BaseComment>
    extends CrudService<COMMENT, Long> {

    /**
     * Lists comments by post id.
     *
     * @param postId post id must not be null
     * @return a list of comment
     */
    @NonNull
    List<COMMENT> listBy(@NonNull Integer postId);

    /**
     * Lists latest comments.
     *
     * @param top top number must not be less than 0
     * @return a page of comments
     */
    @NonNull
    Page<COMMENT> pageLatest(int top);

    /**
     * Lists latest comments by status
     *
     * @param top top number must not be less than 0
     * @param status status
     * @return a page of comments
     */
    @NonNull
    Page<COMMENT> pageLatest(int top, @Nullable CommentStatus status);

    /**
     * Pages comments.
     *
     * @param status comment status must not be null
     * @param pageable page info must not be null
     * @return a page of comment
     */
    @NonNull
    Page<COMMENT> pageBy(@NonNull CommentStatus status, @NonNull Pageable pageable);

    /**
     * Pages comments.
     *
     * @param commentQuery comment query must not be null
     * @param pageable page info must not be null
     * @return a page of comment
     */
    @NonNull
    Page<COMMENT> pageBy(@NonNull CommentQuery commentQuery, @NonNull Pageable pageable);

    /**
     * Lists comment vos by post id.
     *
     * @param postId post id must not be null
     * @param pageable page info must not be null
     * @return a page of comment vo
     */
    @NonNull
    Page<BaseCommentVO> pageVosAllBy(@NonNull Integer postId, @NonNull Pageable pageable);

    /**
     * Lists comment vos by post id.
     *
     * @param postId post id must not be null
     * @param pageable page info must not be null
     * @return a page of comment vo
     */
    @NonNull
    Page<BaseCommentVO> pageVosBy(@NonNull Integer postId, @NonNull Pageable pageable);

    /**
     * Lists comment with parent vo.
     *
     * @param postId post id must not be null
     * @param pageable page info must not be null
     * @return a page of comment with parent vo.
     */
    @NonNull
    Page<BaseCommentWithParentVO> pageWithParentVoBy(@NonNull Integer postId,
        @NonNull Pageable pageable);

    /**
     * Counts by post id collection.
     *
     * @param postIds post id collection
     * @return a count map, key: post id, value: comment count
     */
    @NonNull
    Map<Integer, Long> countByPostIds(@Nullable Collection<Integer> postIds);

    /**
     * Counts by comment status and post id collection.
     *
     * @param status status
     * @param postIds post id collection
     * @return a count map, key: post id, value: comment count
     */
    Map<Integer, Long> countByStatusAndPostIds(@NonNull CommentStatus status,
        @NonNull Collection<Integer> postIds);

    /**
     * Count comments by post id.
     *
     * @param postId post id must not be null.
     * @return comments count
     */
    long countByPostId(@NonNull Integer postId);

    /**
     * Count comments by comment status and post id.
     *
     * @param status status must not be null.
     * @param postId post id must not be null.
     * @return comments count
     */
    long countByStatusAndPostId(@NonNull CommentStatus status, @NonNull Integer postId);

    /**
     * Counts by comment status.
     *
     * @param status comment status must not be null
     * @return comment count
     */
    long countByStatus(@NonNull CommentStatus status);

    /**
     * Creates a comment by comment.
     *
     * @param comment comment must not be null
     * @return created comment
     */
    @NonNull
    @Override
    COMMENT create(@NonNull COMMENT comment);

    /**
     * Creates a comment by comment param.
     *
     * @param commentParam commet param must not be null
     * @return created comment
     */
    @NonNull
    COMMENT createBy(@NonNull BaseCommentParam<COMMENT> commentParam);

    /**
     * Updates comment status.
     *
     * @param commentId comment id must not be null
     * @param status comment status must not be null
     * @return updated comment
     */
    @NonNull
    COMMENT updateStatus(@NonNull Long commentId, @NonNull CommentStatus status);

    /**
     * Updates comment status by ids.
     *
     * @param ids comment ids must not be null
     * @param status comment status must not be null
     * @return updated comments
     */
    @NonNull
    List<COMMENT> updateStatusByIds(@NonNull List<Long> ids, @NonNull CommentStatus status);

    /**
     * Remove comments by post id.
     *
     * @param postId post id must not be null
     * @return a list of comments
     */
    List<COMMENT> removeByPostId(@NonNull Integer postId);

    /**
     * Removes comments in batch.
     *
     * @param ids ids must not be null.
     * @return a list of deleted comment.
     */
    @NonNull
    List<COMMENT> removeByIds(@NonNull Collection<Long> ids);


    /**
     * Target validation.
     *
     * @param targetId target id must not be null (post id, sheet id or journal id)
     */
    void validateTarget(@NonNull Integer targetId);

    /**
     * Lists a page of top comment.
     *
     * @param targetId target id must not be null
     * @param status comment status must not be null
     * @param pageable page info must not be null
     * @return a page of top comment
     */
    @NonNull
    Page<CommentWithHasChildrenVO> pageTopCommentsBy(@NonNull Integer targetId,
        @NonNull CommentStatus status, @NonNull Pageable pageable);

    /**
     * Lists children comments.
     *
     * @param targetId target id must not be null
     * @param commentParentId comment parent id must not be null
     * @param status comment status must not be null
     * @param sort sort info must not be null
     * @return a list of children comment
     */
    @NonNull
    List<COMMENT> listChildrenBy(@NonNull Integer targetId, @NonNull Long commentParentId,
        @NonNull CommentStatus status, @NonNull Sort sort);

    /**
     * Lists children comments.
     *
     * @param targetId target id must not be null
     * @param commentParentId comment parent id must not be null
     * @param sort sort info must not be null
     * @return a list of children comment
     */
    @NonNull
    List<COMMENT> listChildrenBy(@NonNull Integer targetId, @NonNull Long commentParentId,
        @NonNull Sort sort);
}
