package run.halo.app.theme.router.strategy;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static run.halo.app.theme.router.PageUrlUtils.pageNum;
import static run.halo.app.theme.router.PageUrlUtils.totalPage;
import static run.halo.app.theme.router.strategy.ModelConst.DEFAULT_PAGE_SIZE;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.router.PageUrlUtils;
import run.halo.app.theme.router.UrlContextListResult;

/**
 * The {@link IndexRouteStrategy} for generate {@link HandlerFunction} specific to the template
 * <code>index.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class IndexRouteStrategy implements ListPageRouteHandlerStrategy {

    private final PostFinder postFinder;
    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    private Mono<UrlContextListResult<ListedPostVo>> postList(ServerRequest request) {
        String path = request.path();
        return environmentFetcher.fetchPost()
            .map(p -> defaultIfNull(p.getPostPageSize(), DEFAULT_PAGE_SIZE))
            .flatMap(pageSize -> postFinder.list(pageNum(request), pageSize))
            .map(list -> new UrlContextListResult.Builder<ListedPostVo>()
                .listResult(list)
                .nextUrl(PageUrlUtils.nextPageUrl(path, totalPage(list)))
                .prevUrl(PageUrlUtils.prevPageUrl(path))
                .build());
    }

    @Override
    public HandlerFunction<ServerResponse> getHandler() {
        return request -> ServerResponse.ok()
            .render(DefaultTemplateEnum.INDEX.getValue(),
                Map.of("posts", postList(request),
                    ModelConst.TEMPLATE_ID, DefaultTemplateEnum.INDEX.getValue()));
    }

    @Override
    public List<String> getRouterPaths(String pattern) {
        return List.of("/", "/index");
    }

    @Override
    public boolean supports(DefaultTemplateEnum template) {
        return DefaultTemplateEnum.INDEX.equals(template);
    }
}
