package run.halo.app.theme.finders.impl;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Ref;
import run.halo.app.theme.finders.CommentFinder;
import run.halo.app.theme.finders.CommentPublicQueryService;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.vo.CommentVo;
import run.halo.app.theme.finders.vo.ReplyVo;

/**
 * A default implementation of {@link CommentFinder}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("commentFinder")
@RequiredArgsConstructor
public class CommentFinderImpl implements CommentFinder {

    private final CommentPublicQueryService commentPublicQueryService;

    @Override
    public Mono<CommentVo> getByName(String name) {
        return commentPublicQueryService.getByName(name);
    }

    @Override
    public Mono<ListResult<CommentVo>> list(Map<String, String> map, Integer page, Integer size) {
        if (map == null) {
            return commentPublicQueryService.list(null, page, size);
        }
        Ref ref = new Ref();
        ref.setGroup(map.get("group"));
        ref.setVersion(map.get("version"));
        ref.setKind(map.get("kind"));
        ref.setName(map.get("name"));
        return commentPublicQueryService.list(ref, page, size);
    }

    @Override
    public Mono<ListResult<ReplyVo>> listReply(String commentName, Integer page, Integer size) {
        return commentPublicQueryService.listReply(commentName, page, size);
    }
}
