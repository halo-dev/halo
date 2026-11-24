package run.halo.app.theme.router.factories;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static run.halo.app.extension.index.query.Queries.and;
import static run.halo.app.extension.index.query.Queries.equal;
import static run.halo.app.extension.index.query.Queries.isNull;
import static run.halo.app.theme.router.PageUrlUtils.totalPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.i18n.LocaleContextResolver;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactivePresentationExtensionClient;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.localization.ResourceReference;
import run.halo.app.theme.Constant;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.TagFinder;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.finders.vo.TagVo;
import run.halo.app.theme.router.ModelConst;
import run.halo.app.theme.router.PageUrlUtils;
import run.halo.app.theme.router.TitleVisibilityIdentifyCalculator;
import run.halo.app.theme.router.UrlContextListResult;
import run.halo.app.theme.utils.PublicRoutePatterns;

/**
 * The {@link TagPostRouteFactory} for generate {@link RouterFunction} specific to the template <code>tag.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class TagPostRouteFactory implements RouteFactory {

    private final ReactivePresentationExtensionClient client;
    private final SystemConfigFetcher environmentFetcher;
    private final TagFinder tagFinder;
    private final PostFinder postFinder;

    private final TitleVisibilityIdentifyCalculator titleVisibilityIdentifyCalculator;

    private final LocaleContextResolver localeContextResolver;

    @Override
    public RouterFunction<ServerResponse> create(String prefix) {
        var patterns = patterns(prefix);
        return RouterFunctions.route(
                GET(patterns.get(0)).or(GET(patterns.get(1))).and(accept(MediaType.TEXT_HTML)), handlerFunction());
    }

    @Override
    public List<String> publicPatterns(String prefix) {
        return patterns(prefix);
    }

    @Override
    public Mono<Boolean> matches(String prefix, String path, HttpMethod method) {
        if (!PublicRoutePatterns.supportsRead(method)) {
            return Mono.just(false);
        }
        var variables = PublicRoutePatterns.matchAndExtract(path, patterns(prefix));
        if (variables.isEmpty()) {
            return Mono.just(false);
        }
        var slug = variables.get().get("slug");
        if (slug == null || slug.isBlank()) {
            return Mono.just(false);
        }
        return tagExistsBySlug(slug);
    }

    private List<String> patterns(String prefix) {
        return List.of(
                PathUtils.combinePath(prefix, "/{slug}"), PathUtils.combinePath(prefix, "/{slug}/page/{page:\\d+}"));
    }

    private HandlerFunction<ServerResponse> handlerFunction() {
        return request -> tagBySlug(request.pathVariable("slug")).flatMap(tagVo -> {
            int pageNum = pageNumInPathVariable(request);
            String path = request.path();
            var postList = postList(tagVo.getMetadata().getName(), pageNum, path)
                    .doOnNext(list -> list.forEach(postVo -> postVo.getSpec()
                            .setTitle(titleVisibilityIdentifyCalculator.calculateTitle(
                                    postVo.getSpec().getTitle(),
                                    postVo.getSpec().getVisible(),
                                    localeContextResolver
                                            .resolveLocaleContext(request.exchange())
                                            .getLocale()))));
            Map<String, Object> model = new HashMap<>();
            var name = tagVo.getMetadata().getName();
            model.put("name", name);
            model.put(
                    ModelConst.RESOURCE_REFERENCE,
                    new ResourceReference(GroupVersionKind.fromExtension(Tag.class), name));
            model.put("posts", postList);
            model.put("tag", tagVo);
            model.put(ModelConst.TEMPLATE_ID, DefaultTemplateEnum.TAG.getValue());
            model.put(Constant.META_DESCRIPTION_VARIABLE_NAME, tagVo.getSpec().getDescription());
            return ServerResponse.ok().render(DefaultTemplateEnum.TAG.getValue(), model);
        });
    }

    private Mono<UrlContextListResult<ListedPostVo>> postList(String name, Integer page, String requestPath) {
        return configuredPageSize(environmentFetcher, SystemSetting.Post::getTagPageSize)
                .flatMap(pageSize -> postFinder.listByTag(page, pageSize, name))
                .map(list -> new UrlContextListResult.Builder<ListedPostVo>()
                        .listResult(list)
                        .nextUrl(PageUrlUtils.nextPageUrl(requestPath, totalPage(list)))
                        .prevUrl(PageUrlUtils.prevPageUrl(requestPath))
                        .build());
    }

    private Mono<TagVo> tagBySlug(String slug) {
        var listOptions = listOptionsBySlug(slug);
        return client.listBy(Tag.class, listOptions, PageRequestImpl.ofSize(1))
                .mapNotNull(result -> ListResult.first(result).orElse(null))
                .flatMap(tag -> tagFinder.getByName(tag.getMetadata().getName()))
                .switchIfEmpty(Mono.error(new NotFoundException("Tag not found with slug: " + slug)));
    }

    private Mono<Boolean> tagExistsBySlug(String slug) {
        return client.countBy(Tag.class, listOptionsBySlug(slug)).map(count -> count > 0);
    }

    private ListOptions listOptionsBySlug(String slug) {
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(
                FieldSelector.of(and(equal("spec.slug", slug), isNull("metadata.deletionTimestamp"))));
        return listOptions;
    }
}
