package run.halo.app.theme.router.factories;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static run.halo.app.theme.router.PageUrlUtils.totalPage;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.i18n.LocaleContextResolver;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.PostArchiveVo;
import run.halo.app.theme.router.ModelConst;
import run.halo.app.theme.router.PageUrlUtils;
import run.halo.app.theme.router.TitleVisibilityIdentifyCalculator;
import run.halo.app.theme.router.UrlContextListResult;

/**
 * The {@link ArchiveRouteFactory} for generate {@link RouterFunction} specific to the template
 * <code>posts.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class ArchiveRouteFactory implements RouteFactory {

    private final PostFinder postFinder;

    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    private final TitleVisibilityIdentifyCalculator titleVisibilityIdentifyCalculator;

    private final LocaleContextResolver localeContextResolver;

    @Override
    public RouterFunction<ServerResponse> create(String prefix) {
        RequestPredicate requestPredicate = patterns(prefix).stream()
            .map(RequestPredicates::GET)
            .reduce(req -> false, RequestPredicate::or)
            .and(accept(MediaType.TEXT_HTML));
        return RouterFunctions.route(requestPredicate, handlerFunction());
    }

    HandlerFunction<ServerResponse> handlerFunction() {
        return request -> {
            String templateName = DefaultTemplateEnum.ARCHIVES.getValue();
            return ServerResponse.ok()
                .render(templateName,
                    Map.of("archives", archivePosts(request),
                        ModelConst.TEMPLATE_ID, templateName)
                );
        };
    }

    private List<String> patterns(String prefix) {
        return List.of(
            StringUtils.prependIfMissing(prefix, "/"),
            PathUtils.combinePath(prefix, "/page/{page:\\d+}"),
            PathUtils.combinePath(prefix, "/{year:\\d{4}}"),
            PathUtils.combinePath(prefix, "/{year:\\d{4}}/page/{page:\\d+}"),
            PathUtils.combinePath(prefix, "/{year:\\d{4}}/{month:\\d{2}}"),
            PathUtils.combinePath(prefix,
                "/{year:\\d{4}}/{month:\\d{2}}/page/{page:\\d+}")
        );
    }

    private Mono<UrlContextListResult<PostArchiveVo>> archivePosts(ServerRequest request) {
        ArchivePathVariables variables = ArchivePathVariables.from(request);
        int pageNum = pageNumInPathVariable(request);
        String requestPath = request.path();
        return configuredPageSize(environmentFetcher, SystemSetting.Post::getArchivePageSize)
            .flatMap(pageSize -> postFinder.archives(pageNum, pageSize, variables.getYear(),
                variables.getMonth()))
            .doOnNext(list -> list.get()
                .map(PostArchiveVo::getMonths)
                .flatMap(List::stream)
                .flatMap(month -> month.getPosts().stream())
                .forEach(postVo -> postVo.getSpec()
                    .setTitle(titleVisibilityIdentifyCalculator.calculateTitle(
                        postVo.getSpec().getTitle(),
                        postVo.getSpec().getVisible(),
                        localeContextResolver.resolveLocaleContext(request.exchange())
                            .getLocale())
                    )
                )
            )
            .map(list -> new UrlContextListResult.Builder<PostArchiveVo>()
                .listResult(list)
                .nextUrl(PageUrlUtils.nextPageUrl(requestPath, totalPage(list)))
                .prevUrl(PageUrlUtils.prevPageUrl(requestPath))
                .build());
    }

    @Data
    static class ArchivePathVariables {
        String year;
        String month;
        String page;

        static ArchivePathVariables from(ServerRequest request) {
            Map<String, String> variables = request.pathVariables();
            return JsonUtils.mapToObject(variables, ArchivePathVariables.class);
        }
    }
}
