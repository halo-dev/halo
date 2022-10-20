package run.halo.app.theme.router.strategy;

import static run.halo.app.theme.router.PageUrlUtils.pageNum;
import static run.halo.app.theme.router.PageUrlUtils.totalPage;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.PostArchiveVo;
import run.halo.app.theme.router.PageUrlUtils;
import run.halo.app.theme.router.UrlContextListResult;

/**
 * The {@link ArchivesRouteStrategy} for generate {@link HandlerFunction} specific to the template
 * <code>posts.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class ArchivesRouteStrategy implements ListPageRouteHandlerStrategy {
    private final PostFinder postFinder;

    public ArchivesRouteStrategy(PostFinder postFinder) {
        this.postFinder = postFinder;
    }

    private Mono<UrlContextListResult<PostArchiveVo>> postList(ServerRequest request) {
        String year = request.pathVariable("year");
        String month = request.pathVariable("month");
        String path = request.path();
        return Mono.defer(() -> Mono.just(postFinder.archives(pageNum(request), 10, year, month)))
            .publishOn(Schedulers.boundedElastic())
            .map(list -> new UrlContextListResult.Builder<PostArchiveVo>()
                .listResult(list)
                .nextUrl(PageUrlUtils.nextPageUrl(path, totalPage(list)))
                .prevUrl(PageUrlUtils.prevPageUrl(path))
                .build());
    }

    @Override
    public HandlerFunction<ServerResponse> getHandler() {
        return request -> ServerResponse.ok()
            .render(DefaultTemplateEnum.ARCHIVES.getValue(),
                Map.of("archives", postList(request)));
    }

    @Override
    public List<String> getRouterPaths(String prefix) {
        return List.of(
            prefix,
            PathUtils.combinePath(prefix, "/page/{page:\\d+}"),
            PathUtils.combinePath(prefix, "/{year:\\d{4}}"),
            PathUtils.combinePath(prefix, "/{year:\\d{4}}/page/{page:\\d+}"),
            PathUtils.combinePath(prefix, "/{year:\\d{4}}/{month:\\d{2}}"),
            PathUtils.combinePath(prefix,
                "/{year:\\d{4}}/{month:\\d{2}}/page/{page:\\d+}")
        );
    }

    @Override
    public boolean supports(DefaultTemplateEnum template) {
        return DefaultTemplateEnum.ARCHIVES.equals(template);
    }
}
