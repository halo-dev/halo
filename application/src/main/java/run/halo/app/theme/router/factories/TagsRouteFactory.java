package run.halo.app.theme.router.factories;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import java.util.Map;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.TagFinder;

/**
 * The {@link TagsRouteFactory} for generate {@link RouterFunction} specific to the template
 * <code>tags.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class TagsRouteFactory implements RouteFactory {

    private final TagFinder tagFinder;

    @Override
    public RouterFunction<ServerResponse> create(String prefix) {
        return RouterFunctions
            .route(GET(StringUtils.prependIfMissing(prefix, "/"))
                .and(accept(MediaType.TEXT_HTML)), handlerFunction());
    }

    private HandlerFunction<ServerResponse> handlerFunction() {
        return request -> ServerResponse.ok()
            .render(DefaultTemplateEnum.TAGS.getValue(),
                Map.of("tags", tagFinder.listAll(),
                    ModelConst.TEMPLATE_ID, DefaultTemplateEnum.TAGS.getValue()
                )
            );
    }
}
