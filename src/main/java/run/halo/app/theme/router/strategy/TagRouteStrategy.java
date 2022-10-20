package run.halo.app.theme.router.strategy;

import static run.halo.app.theme.router.PageUrlUtils.pageNum;
import static run.halo.app.theme.router.PageUrlUtils.totalPage;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.Tag;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.TagFinder;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.finders.vo.TagVo;
import run.halo.app.theme.router.PageUrlUtils;
import run.halo.app.theme.router.UrlContextListResult;

/**
 * The {@link TagRouteStrategy} for generate {@link RouterFunction} specific to the template
 * <code>tag.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class TagRouteStrategy implements DetailsPageRouteHandlerStrategy {
    private final GroupVersionKind gvk = GroupVersionKind.fromExtension(Tag.class);
    private final PostFinder postFinder;

    private final TagFinder tagFinder;

    public TagRouteStrategy(PostFinder postFinder,
        TagFinder tagFinder) {
        this.postFinder = postFinder;
        this.tagFinder = tagFinder;
    }

    private Mono<UrlContextListResult<PostVo>> postList(ServerRequest request, String name) {
        String path = request.path();
        return Mono.defer(() -> Mono.just(postFinder.listByTag(pageNum(request), 10, name)))
            .publishOn(Schedulers.boundedElastic())
            .map(list -> new UrlContextListResult.Builder<PostVo>()
                .listResult(list)
                .nextUrl(PageUrlUtils.nextPageUrl(path, totalPage(list)))
                .prevUrl(PageUrlUtils.prevPageUrl(path))
                .build());
    }

    private Mono<TagVo> tagByName(String name) {
        return Mono.defer(() -> Mono.just(tagFinder.getByName(name)))
            .publishOn(Schedulers.boundedElastic());
    }

    @Override
    public HandlerFunction<ServerResponse> getHandler(SystemSetting.ThemeRouteRules routeRules,
        String name) {
        return request -> ServerResponse.ok()
            .render(DefaultTemplateEnum.TAG.getValue(),
                Map.of("name", name,
                    "posts", postList(request, name),
                    "tag", tagByName(name))
            );
    }

    @Override
    public boolean supports(GroupVersionKind gvk) {
        return this.gvk.equals(gvk);
    }
}
