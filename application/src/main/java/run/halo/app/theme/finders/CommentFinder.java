package run.halo.app.theme.finders;

import java.util.Map;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.extension.ListResult;
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

    Mono<ListResult<CommentVo>> list(@Nullable Map<String, String> ref, @Nullable Integer page, @Nullable Integer size);

    /**
     * Lists comments by query params. Omit ref to list comments across subjects.
     *
     * @param params query params see {@link run.halo.app.theme.finders.impl.CommentFinderImpl.CommentQuery}
     */
    Mono<ListResult<CommentVo>> list(@Nullable Map<String, Object> params);

    Mono<ListResult<ReplyVo>> listReply(String commentName, @Nullable Integer page, @Nullable Integer size);

    /**
     * Lists replies to a comment by query params.
     *
     * @param commentName the comment name
     * @param params query params see {@link run.halo.app.theme.finders.impl.CommentFinderImpl.ReplyQuery}
     */
    Mono<ListResult<ReplyVo>> listReply(String commentName, @Nullable Map<String, Object> params);
}
