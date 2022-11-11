package run.halo.app.theme.router.strategy;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.TagFinder;

/**
 * The {@link TagsRouteStrategy} for generate {@link HandlerFunction} specific to the template
 * <code>tags.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class TagsRouteStrategy implements ListPageRouteHandlerStrategy {

    private final TagFinder tagFinder;

    public TagsRouteStrategy(TagFinder tagFinder) {
        this.tagFinder = tagFinder;
    }

    @Override
    public HandlerFunction<ServerResponse> getHandler() {
        return request -> ServerResponse.ok()
            .render(DefaultTemplateEnum.TAGS.getValue(),
                Map.of("tags", tagFinder.listAll(),
                    ModelConst.TEMPLATE_ID, DefaultTemplateEnum.TAGS.getValue()
                )
            );
    }

    @Override
    public List<String> getRouterPaths(String prefix) {
        return List.of(StringUtils.prependIfMissing(prefix, "/"));
    }

    @Override
    public boolean supports(DefaultTemplateEnum template) {
        return DefaultTemplateEnum.TAGS.equals(template);
    }
}
