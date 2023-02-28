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
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.TagFinder;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.finders.vo.TagVo;
import run.halo.app.theme.router.PageUrlUtils;
import run.halo.app.theme.router.UrlContextListResult;

/**
 * The {@link TagPostRouteFactory} for generate {@link RouterFunction} specific to the template
 * <code>tag.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class TagPostRouteFactory implements RouteFactory {

    private final ReactiveExtensionClient client;
    private final SystemConfigurableEnvironmentFetcher environmentFetcher;
    private final TagFinder tagFinder;
    private final PostFinder postFinder;

    @Override
    public RouterFunction<ServerResponse> create(String prefix) {
        return RouterFunctions
            .route(GET(PathUtils.combinePath(prefix, "/{slug}"))
                .or(GET(PathUtils.combinePath(prefix, "/{slug}/page/{page:\\d+}")))
                .and(accept(MediaType.TEXT_HTML)), handlerFunction());
    }

    private HandlerFunction<ServerResponse> handlerFunction() {
        return request -> tagBySlug(request.pathVariable("slug"))
            .flatMap(tagVo -> {
                int pageNum = pageNumInPathVariable(request);
                String path = request.path();
                var postList = postList(tagVo.getMetadata().getName(), pageNum, path);
                return ServerResponse.ok()
                    .render(DefaultTemplateEnum.TAG.getValue(),
                        Map.of("name", tagVo.getMetadata().getName(),
                            "posts", postList,
                            "tag", tagVo)
                    );
            });
    }

    private Mono<UrlContextListResult<ListedPostVo>> postList(String name, Integer page,
        String requestPath) {
        return configuredPageSize(environmentFetcher, SystemSetting.Post::getTagPageSize)
            .flatMap(pageSize -> postFinder.listByTag(page, pageSize, name))
            .map(list -> new UrlContextListResult.Builder<ListedPostVo>()
                .listResult(list)
                .nextUrl(PageUrlUtils.nextPageUrl(requestPath, totalPage(list)))
                .prevUrl(PageUrlUtils.prevPageUrl(requestPath))
                .build()
            );
    }

    private Mono<TagVo> tagBySlug(String slug) {
        return client.list(Tag.class, tag -> tag.getSpec().getSlug().equals(slug)
                && tag.getMetadata().getDeletionTimestamp() == null, null)
            .next()
            .flatMap(tag -> tagFinder.getByName(tag.getMetadata().getName()))
            .switchIfEmpty(
                Mono.error(new NotFoundException("Tag not found with slug: " + slug)));
    }

}
