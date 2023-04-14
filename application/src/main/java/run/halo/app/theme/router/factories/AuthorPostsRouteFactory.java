package run.halo.app.theme.router.factories;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static run.halo.app.theme.router.PageUrlUtils.totalPage;

import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.finders.vo.UserVo;
import run.halo.app.theme.router.PageUrlUtils;
import run.halo.app.theme.router.UrlContextListResult;

/**
 * The {@link AuthorPostsRouteFactory} for generate {@link RouterFunction} specific to the template
 * <code>index.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class AuthorPostsRouteFactory implements RouteFactory {

    private final PostFinder postFinder;
    private final ReactiveExtensionClient client;
    private SystemConfigurableEnvironmentFetcher environmentFetcher;

    @Override
    public RouterFunction<ServerResponse> create(String pattern) {
        return RouterFunctions
            .route(GET("/authors/{name}").or(GET("/authors/{name}/page/{page}"))
                .and(accept(MediaType.TEXT_HTML)), handlerFunction());
    }

    HandlerFunction<ServerResponse> handlerFunction() {
        return request -> {
            String name = request.pathVariable("name");
            return ServerResponse.ok()
                .render(DefaultTemplateEnum.AUTHOR.getValue(),
                    Map.of("author", getByName(name),
                        "posts", postList(request, name),
                        ModelConst.TEMPLATE_ID, DefaultTemplateEnum.AUTHOR.getValue()
                    )
                );
        };
    }

    private Mono<UrlContextListResult<ListedPostVo>> postList(ServerRequest request, String name) {
        String path = request.path();
        int pageNum = pageNumInPathVariable(request);
        return configuredPageSize(environmentFetcher, SystemSetting.Post::getPostPageSize)
            .flatMap(pageSize -> postFinder.listByOwner(pageNum, pageSize, name))
            .map(list -> new UrlContextListResult.Builder<ListedPostVo>()
                .listResult(list)
                .nextUrl(PageUrlUtils.nextPageUrl(path, totalPage(list)))
                .prevUrl(PageUrlUtils.prevPageUrl(path))
                .build());
    }

    private Mono<UserVo> getByName(String name) {
        return client.fetch(User.class, name)
            .switchIfEmpty(Mono.error(() -> new NotFoundException("Author page not found.")))
            .map(UserVo::from);
    }
}
