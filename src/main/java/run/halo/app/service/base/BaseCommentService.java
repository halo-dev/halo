package run.halo.app.service.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.model.entity.BaseComment;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.params.CommentQuery;
import run.halo.app.model.vo.BaseCommentVO;
import run.halo.app.model.vo.BaseCommentWithParentVO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Base comment service interface.
 *
 * @author johnniang
 * @date 19-4-24
 */
public interface BaseCommentService<COMMENT extends BaseComment> extends CrudService<COMMENT, Long> {

    /**
     * %d: parent commentator id
     * %s: parent commentator author name
     * %s: comment content
     */
    String COMMENT_TEMPLATE = "<a href='#comment-id-%d'>@%s</a> %s";

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
     * Pages comments.
     *
     * @param status   comment status must not be null
     * @param pageable page info must not be null
     * @return a page of comment
     */
    @NonNull
    Page<COMMENT> pageBy(@NonNull CommentStatus status, @NonNull Pageable pageable);

    /**
     * Pages comments.
     *
     * @param commentQuery comment query must not be null
     * @param pageable     page info must not be null
     * @return a page of comment
     */
    @NonNull
    Page<COMMENT> pageBy(@NonNull CommentQuery commentQuery, @NonNull Pageable pageable);

    /**
     * Lists comment vos by post id.
     *
     * @param postId   post id must not be null
     * @param pageable page info must not be null
     * @return a page of comment vo
     */
    @NonNull
    Page<BaseCommentVO> pageVosBy(@NonNull Integer postId, @NonNull Pageable pageable);

    /**
     * Lists comment with parent vo.
     *
     * @param postId   post id must not be null
     * @param pageable page info must not be null
     * @return a page of comment with parent vo.
     */
    @NonNull
    Page<BaseCommentWithParentVO> pageWithParentVoBy(@NonNull Integer postId, @NonNull Pageable pageable);

    /**
     * Counts by post id collection.
     *
     * @param postIds post id collection
     * @return a count map, key: post id, value: comment count
     */
    @NonNull
    Map<Integer, Long> countByPostIds(@Nullable Collection<Integer> postIds);

    /**
     * Creates a comment by comment param.
     *
     * @param COMMENT comment must not be null
     * @return created comment
     */
    @NonNull
    COMMENT createBy(@NonNull COMMENT COMMENT);

    /**
     * Updates comment status.
     *
     * @param commentId comment id must not be null
     * @param status    comment status must not be null
     * @return updated comment
     */
    @NonNull
    COMMENT updateStatus(@NonNull Long commentId, @NonNull CommentStatus status);
}
