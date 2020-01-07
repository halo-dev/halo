package run.halo.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.model.entity.PostComment;
import run.halo.app.model.params.CommentQuery;
import run.halo.app.model.vo.PostCommentWithPostVO;
import run.halo.app.service.base.BaseCommentService;

import java.util.List;

/**
 * Post comment service interface.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-14
 */
public interface PostCommentService extends BaseCommentService<PostComment> {

    /**
     * Converts to with post vo.
     *
     * @param commentPage comment page must not be null
     * @return a page of comment with post vo
     */
    @NonNull
    Page<PostCommentWithPostVO> convertToWithPostVo(@NonNull Page<PostComment> commentPage);

    /**
     * Converts to with post vo
     *
     * @param comment comment
     * @return a comment with post vo
     */
    @NonNull
    PostCommentWithPostVO convertToWithPostVo(@NonNull PostComment comment);

    /**
     * Converts to with post vo
     *
     * @param postComments comment list
     * @return a list of comment with post vo
     */
    @NonNull
    List<PostCommentWithPostVO> convertToWithPostVo(@Nullable List<PostComment> postComments);

    @NonNull
    Page<PostCommentWithPostVO> pageTreeBy(@NonNull CommentQuery commentQuery, @NonNull Pageable pageable);
}
