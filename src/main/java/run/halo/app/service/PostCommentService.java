package run.halo.app.service;

import run.halo.app.model.entity.PostComment;
import run.halo.app.service.base.BaseCommentService;

/**
 * Post comment service interface.
 *
 * @author johnniang
 * @author ryanwang
 * @author guqing
 * @date 2019-03-14
 */
public interface PostCommentService extends BaseCommentService<PostComment> {

    /**
     * Validate CommentBlackList Status.
     */
    void validateCommentBlackListStatus();
}
