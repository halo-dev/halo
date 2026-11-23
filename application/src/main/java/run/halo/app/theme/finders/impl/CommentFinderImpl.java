package run.halo.app.theme.finders.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.Ref;
import run.halo.app.infra.utils.JacksonUtils;
import run.halo.app.infra.utils.SortUtils;
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

    @Override
    public Mono<ListResult<CommentVo>> list(Map<String, Object> params) {
        var query = Optional.ofNullable(params)
                .map(map -> JacksonUtils.mapToObject(map, CommentQuery.class))
                .orElseGet(CommentQuery::new);
        return commentPublicQueryService.list(query.getRef(), query.toPageRequest());
    }

    @Override
    public Mono<ListResult<ReplyVo>> listReply(String commentName, Map<String, Object> params) {
        var query = Optional.ofNullable(params)
                .map(map -> JacksonUtils.mapToObject(map, ReplyQuery.class))
                .orElseGet(ReplyQuery::new);
        return commentPublicQueryService.listReply(commentName, query.toPageRequest());
    }

    @Data
    public static class CommentQuery {
        private Ref ref;
        private Integer page;
        private Integer size;

        /** Sort expressions such as spec.creationTime,desc, followed by the default comment sort. */
        private List<String> sort;

        public PageRequest toPageRequest() {
            return PageRequestImpl.of(
                    ObjectUtils.defaultIfNull(page, 1), ObjectUtils.defaultIfNull(size, 10), SortUtils.resolve(sort));
        }
    }

    @Data
    public static class ReplyQuery {
        private Integer page;
        private Integer size;

        /** Sort expressions such as spec.creationTime,desc, followed by the default reply sort. */
        private List<String> sort;

        public PageRequest toPageRequest() {
            return PageRequestImpl.of(
                    ObjectUtils.defaultIfNull(page, 1), ObjectUtils.defaultIfNull(size, 10), SortUtils.resolve(sort));
        }
    }
}
