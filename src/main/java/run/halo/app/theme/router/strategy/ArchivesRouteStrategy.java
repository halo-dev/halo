package run.halo.app.theme.router.strategy;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static run.halo.app.theme.router.PageUrlUtils.pageNum;
import static run.halo.app.theme.router.PageUrlUtils.totalPage;
import static run.halo.app.theme.router.strategy.ModelConst.DEFAULT_PAGE_SIZE;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.extension.ListResult;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
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
    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    public ArchivesRouteStrategy(PostFinder postFinder,
        SystemConfigurableEnvironmentFetcher environmentFetcher) {
        this.postFinder = postFinder;
        this.environmentFetcher = environmentFetcher;
    }

    private Mono<UrlContextListResult<PostArchiveVo>> postList(ServerRequest request) {
        String year = pathVariable(request, "year");
        String month = pathVariable(request, "month");
        String path = request.path();
        return environmentFetcher.fetchPost()
            .map(postSetting -> defaultIfNull(postSetting.getArchivePageSize(), DEFAULT_PAGE_SIZE))
            .flatMap(pageSize -> listPost(pageNum(request), pageSize, year, month))
            .map(list -> new UrlContextListResult.Builder<PostArchiveVo>()
                .listResult(list)
                .nextUrl(PageUrlUtils.nextPageUrl(path, totalPage(list)))
                .prevUrl(PageUrlUtils.prevPageUrl(path))
                .build());
    }

    Mono<ListResult<PostArchiveVo>> listPost(int pageNum, int pageSize, String year, String month) {
        return Mono.fromCallable(() -> postFinder.archives(pageNum, pageSize, year, month))
            .subscribeOn(Schedulers.boundedElastic());
    }

    private String pathVariable(ServerRequest request, String name) {
        Map<String, String> pathVariables = request.pathVariables();
        if (pathVariables.containsKey(name)) {
            return pathVariables.get(name);
        }
        return null;
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
            StringUtils.prependIfMissing(prefix, "/"),
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
