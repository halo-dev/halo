package run.halo.app.theme.router.strategy;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.Post;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.PostVo;

/**
 * The {@link PostRouteStrategy} for generate {@link HandlerFunction} specific to the template
 * <code>post.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PostRouteStrategy implements DetailsPageRouteHandlerStrategy {
    static final String NAME_PARAM = "name";
    private final GroupVersionKind groupVersionKind = GroupVersionKind.fromExtension(Post.class);
    private final PostFinder postFinder;

    public PostRouteStrategy(PostFinder postFinder) {
        this.postFinder = postFinder;
    }

    @Override
    public HandlerFunction<ServerResponse> getHandler(SystemSetting.ThemeRouteRules routeRules,
        final String name) {
        return request -> {
            String pattern = routeRules.getPost();
            final GVK gvk = Post.class.getAnnotation(GVK.class);
            PathPattern parse = PathPatternParser.defaultInstance.parse(pattern);
            PathPattern.PathMatchInfo pathMatchInfo =
                parse.matchAndExtract(PathContainer.parsePath(request.path()));
            Map<String, Object> model = new HashMap<>();
            model.put(NAME_PARAM, name);
            if (pathMatchInfo != null) {
                model.putAll(pathMatchInfo.getUriVariables());
            }
            model.put("post", postByName(name));
            model.put("groupVersionKind", groupVersionKind);
            model.put("plural", gvk.plural());
            return ServerResponse.ok()
                .render(DefaultTemplateEnum.POST.getValue(), model);
        };
    }

    @Override
    public boolean supports(GroupVersionKind gvk) {
        return groupVersionKind.equals(gvk);
    }

    private Mono<PostVo> postByName(String name) {
        return Mono.defer(() -> Mono.just(postFinder.getByName(name)))
            .publishOn(Schedulers.boundedElastic());
    }
}
