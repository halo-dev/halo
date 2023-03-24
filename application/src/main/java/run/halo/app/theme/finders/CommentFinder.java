package run.halo.app.theme.finders;

import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Comment;
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

    Mono<CommentVo> getByName(String name);

    Mono<ListResult<CommentVo>> list(Ref ref, @Nullable Integer page,
        @Nullable Integer size);

    Mono<ListResult<ReplyVo>> listReply(String commentName, @Nullable Integer page,
        @Nullable Integer size);
}
