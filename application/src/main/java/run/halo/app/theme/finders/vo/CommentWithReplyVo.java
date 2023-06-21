package run.halo.app.theme.finders.vo;

import lombok.Builder;
import lombok.Data;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.extension.ListResult;

/**
 * A value object for {@link Comment} with one page of replies.
 *
 * @author guqing
 * @since 2.7.0
 */
@Data
@Builder
public class CommentWithReplyVo {

    private CommentVo comment;

    private ListResult<ReplyVo> replies;
}
