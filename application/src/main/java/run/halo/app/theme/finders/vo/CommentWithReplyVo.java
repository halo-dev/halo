package run.halo.app.theme.finders.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;
import run.halo.app.extension.ListResult;

/**
 * <p>A value object for comment with reply.</p>
 *
 * @author guqing
 * @since 2.14.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class CommentWithReplyVo extends CommentVo {

    private ListResult<ReplyVo> replies;

    /**
     * Convert {@link CommentVo} to {@link CommentWithReplyVo}.
     */
    public static CommentWithReplyVo from(CommentVo commentVo) {
        var commentWithReply = new CommentWithReplyVo();
        BeanUtils.copyProperties(commentVo, commentWithReply);
        commentWithReply.setReplies(ListResult.emptyResult());
        return commentWithReply;
    }
}
