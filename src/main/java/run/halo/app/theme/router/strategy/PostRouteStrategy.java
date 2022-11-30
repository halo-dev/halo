package run.halo.app.theme.router.strategy;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.router.ViewNameResolver;

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
    private final ViewNameResolver viewNameResolver;

    public PostRouteStrategy(PostFinder postFinder, ViewNameResolver viewNameResolver) {
        this.postFinder = postFinder;
        this.viewNameResolver = viewNameResolver;
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
            // used by HaloTrackerProcessor
            model.put("groupVersionKind", groupVersionKind);
            model.put("plural", gvk.plural());
            // used by TemplateGlobalHeadProcessor and PostTemplateHeadProcessor
            model.put(ModelConst.TEMPLATE_ID, DefaultTemplateEnum.POST.getValue());
            return postFinder.getByName(name)
                .flatMap(postVo -> {
                    model.put("post", postVo);
                    String template = postVo.getSpec().getTemplate();
                    return viewNameResolver.resolveViewNameOrDefault(request, template,
                            DefaultTemplateEnum.POST.getValue())
                        .flatMap(templateName -> ServerResponse.ok().render(templateName, model));
                });
        };
    }

    @Override
    public boolean supports(GroupVersionKind gvk) {
        return groupVersionKind.equals(gvk);
    }
}
