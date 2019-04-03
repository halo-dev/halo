package run.halo.app.service;

import run.halo.app.model.entity.Comment;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.vo.CommentWithParentVO;
import run.halo.app.model.vo.CommentWithPostVO;
import run.halo.app.model.vo.CommentVO;
import run.halo.app.service.base.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.service.base.CrudService;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Comment service.
 *
 * @author johnniang
 */
public interface CommentService extends CrudService<Comment, Long> {

    /**
     * %d: parent commentator id
     * %s: parent commentator author name
     * %s: comment content
     */
    String COMMENT_TEMPLATE = "<a href='#comment-id-%d'>@%s</a> %s";

    /**
     * Lists latest comments.
     *
     * @param top top number must not be less than 0
     * @return a page of comments
     */
    @NonNull
    Page<CommentWithPostVO> pageLatest(int top);

    /**
     * Pages comments.
     *
     * @param status   comment status must not be null
     * @param pageable page info must not be null
     * @return a page of comment
     */
    @NonNull
    Page<CommentWithPostVO> pageBy(@NonNull CommentStatus status, @NonNull Pageable pageable);

    /**
     * Lists comments by post id.
     *
     * @param postId post id must not be null
     * @return a list of comment
     */
    @NonNull
    List<Comment> listBy(@NonNull Integer postId);

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
     * @param comment comment must not be null
     * @param request http servlet request must not be null
     * @return created comment
     */
    @NonNull
    Comment createBy(@NonNull Comment comment, @NonNull HttpServletRequest request);

    /**
     * Lists comment vos by post id.
     *
     * @param postId   post id must not be null
     * @param pageable page info must not be null
     * @return a page of comment vo
     */
    @NonNull
    Page<CommentVO> pageVosBy(@NonNull Integer postId, @NonNull Pageable pageable);

    /**
     * Lists comment with parent vo.
     *
     * @param postId   post id must not be null
     * @param pageable page info must not be null
     * @return a page of comment with parent vo.
     */
    @NonNull
    Page<CommentWithParentVO> pageWithParentVoBy(@NonNull Integer postId, @NonNull Pageable pageable);

    /**
     * Updates comment status.
     *
     * @param commentId comment id must not be null
     * @param status    comment status must not be null
     * @return updated comment
     */
    @NonNull
    Comment updateStatus(@NonNull Long commentId, @NonNull CommentStatus status);
}
