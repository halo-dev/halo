package run.halo.app.theme.router.strategy;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static run.halo.app.theme.router.PageUrlUtils.pageNum;
import static run.halo.app.theme.router.PageUrlUtils.totalPage;

import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.finders.vo.UserVo;
import run.halo.app.theme.router.PageUrlUtils;
import run.halo.app.theme.router.UrlContextListResult;

/**
 * Author route strategy.
 *
 * @author guqing
 * @since 2.0.1
 */
@Component
@AllArgsConstructor
public class AuthorRouteStrategy implements DetailsPageRouteHandlerStrategy {

    private final ReactiveExtensionClient client;

    private final PostFinder postFinder;

    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    @Override
    public HandlerFunction<ServerResponse> getHandler(SystemSetting.ThemeRouteRules routeRules,
        String name) {
        return request -> ServerResponse.ok()
            .render(DefaultTemplateEnum.AUTHOR.getValue(),
                Map.of("name", name,
                    "author", getByName(name),
                    "posts", postList(request, name),
                    ModelConst.TEMPLATE_ID, DefaultTemplateEnum.AUTHOR.getValue()
                )
            );
    }

    private Mono<UrlContextListResult<ListedPostVo>> postList(ServerRequest request, String name) {
        String path = request.path();
        return environmentFetcher.fetchPost()
            .map(p -> defaultIfNull(p.getPostPageSize(), ModelConst.DEFAULT_PAGE_SIZE))
            .flatMap(pageSize -> postFinder.listByOwner(pageNum(request), pageSize, name))
            .map(list -> new UrlContextListResult.Builder<ListedPostVo>()
                .listResult(list)
                .nextUrl(PageUrlUtils.nextPageUrl(path, totalPage(list)))
                .prevUrl(PageUrlUtils.prevPageUrl(path))
                .build());
    }

    private Mono<UserVo> getByName(String name) {
        return client.fetch(User.class, name)
            .map(UserVo::from);
    }

    @Override
    public boolean supports(GroupVersionKind gvk) {
        return GroupVersionKind.fromExtension(User.class).equals(gvk);
    }
}
