package run.halo.app.content;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import run.halo.app.extension.GroupVersionKind;

public class RouteChangeCommand {
    private PermalinkIndexer permalinkIndexer;
    Map<String, RouterFunction<ServerResponse>> routerFunctionMap = new HashMap<>();

    public void changeRoute(String templateName, String pattern) {
        routerFunctionMap.remove(templateName);
        routerFunctionMap.put(templateName, post(pattern));
        // pattern resourceName permalink templateName
        // pattern -> resourceName
        // pattern -> templateName
        // request url -> permalink -> pattern(path variable)
        // register router function

        // template name -> patterns
        // such as: archives -> /archives/{year}/{month}/{day},/archives,/archives/page/{page}
    }

    public void test() {
        // Map<String, RouterFunction> templateName, routerFunction
        // rule changed -> remove by template name -> register by template name
        routerFunctionMap.put("archives", archives("archives"));
        routerFunctionMap.put("post", archives("/{year}/{month}/{day}/{slug}"));
    }

    RouterFunction<ServerResponse> archives(String prefix) {
        return RouterFunctions
            .route(GET(prefix)
                    .or(GET(prefix + "/page/{page}"))
                    .or(GET(prefix + "/{year}/{month}"))
                    .or(GET(prefix + "/{year}/{month}/page/{page}"))
                    .and(accept(MediaType.TEXT_HTML)),
                request -> ServerResponse.ok().render("archives"));
    }

    RouterFunction<ServerResponse> post(String pattern) {
        List<String> permalinks =
            permalinkIndexer.getPermalinks(GroupVersionKind.fromAPIVersionAndKind("v1", "Post"));
        RequestPredicate predicate = request -> false;
        ;
        for (String permalink : permalinks) {
            predicate.or(GET(permalink));
        }
        return RouterFunctions
            .route(predicate.and(accept(MediaType.TEXT_HTML)),
                request -> {
                    PathPattern parse = PathPatternParser.defaultInstance.parse(pattern);
                    PathPattern.PathMatchInfo pathMatchInfo =
                        parse.matchAndExtract(PathContainer.parsePath(request.path()));
                    Map<String, String> uriVariables = Map.of();
                    if (pathMatchInfo != null) {
                        uriVariables = pathMatchInfo.getUriVariables();
                    }
                    return ServerResponse.ok().render("post", uriVariables);
                });
    }
}
