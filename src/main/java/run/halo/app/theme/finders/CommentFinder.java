package run.halo.app.theme.finders;

import org.springframework.lang.Nullable;
import run.halo.app.core.extension.Comment;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Ref;
import run.halo.app.theme.finders.vo.CommentVo;
import run.halo.app.theme.finders.vo.ReplyVo;

/**
 * A finder for finding {@link Comment comments} in template.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface CommentFinder {

    CommentVo getByName(String name);

    ListResult<CommentVo> list(Ref ref, @Nullable Integer page,
        @Nullable Integer size);

    ListResult<ReplyVo> listReply(String commentName, @Nullable Integer page,
        @Nullable Integer size);
}
