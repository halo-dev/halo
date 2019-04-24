package run.halo.app.service;

import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.model.entity.Comment;
import run.halo.app.model.params.CommentParam;
import run.halo.app.model.vo.CommentWithPostVO;
import run.halo.app.service.base.BaseCommentService;

import java.util.List;

/**
 * Comment service.
 *
 * @author johnniang
 */
public interface CommentService extends BaseCommentService<Comment> {

    /**
     * Creates a comment by comment param.
     *
     * @param commentParam comment param must not be null
     * @return created comment
     */
    @NonNull
    Comment createBy(@NonNull CommentParam commentParam);

    /**
     * Converts to with post vo.
     *
     * @param commentPage comment page must not be null
     * @return a page of comment with post vo
     */
    @NonNull
    Page<CommentWithPostVO> convertToWithPostVo(@NonNull Page<Comment> commentPage);

    /**
     * Converts to with post vo
     *
     * @param comments comment list
     * @return a list of comment with post vo
     */
    @NonNull
    List<CommentWithPostVO> convertToWithPostVo(@Nullable List<Comment> comments);
}
