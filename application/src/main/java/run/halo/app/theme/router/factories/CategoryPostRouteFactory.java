package run.halo.app.theme.router.factories;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static run.halo.app.extension.index.query.Queries.*;
import static run.halo.app.theme.router.PageUrlUtils.totalPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.i18n.LocaleContextResolver;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Category;
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
import run.halo.app.theme.ViewNameResolver;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.CategoryVo;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.router.ModelConst;
import run.halo.app.theme.router.PageUrlUtils;
import run.halo.app.theme.router.TitleVisibilityIdentifyCalculator;
import run.halo.app.theme.router.UrlContextListResult;
import run.halo.app.theme.utils.PublicRoutePatterns;

/**
 * The {@link CategoryPostRouteFactory} for generate {@link RouterFunction} specific to the template <code>category.html
 * </code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class CategoryPostRouteFactory implements RouteFactory {

    private final PostFinder postFinder;

    private final SystemConfigFetcher environmentFetcher;
    private final ReactivePresentationExtensionClient client;
    private final ViewNameResolver viewNameResolver;

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
        return categoryExistsBySlug(slug);
    }

    private List<String> patterns(String prefix) {
        return List.of(
                PathUtils.combinePath(prefix, "/{slug}"), PathUtils.combinePath(prefix, "/{slug}/page/{page:\\d+}"));
    }

    HandlerFunction<ServerResponse> handlerFunction() {
        return request -> {
            String slug = request.pathVariable("slug");
            return fetchBySlug(slug)
                    .flatMap(categoryVo -> {
                        Map<String, Object> model = new HashMap<>();
                        var name = categoryVo.getMetadata().getName();
                        model.put("name", name);
                        model.put(
                                ModelConst.RESOURCE_REFERENCE,
                                new ResourceReference(GroupVersionKind.fromExtension(Category.class), name));
                        model.put(ModelConst.TEMPLATE_ID, DefaultTemplateEnum.CATEGORY.getValue());
                        model.put("posts", postListByCategoryName(name, request));
                        model.put("category", categoryVo);
                        model.put(
                                Constant.META_DESCRIPTION_VARIABLE_NAME,
                                categoryVo.getSpec().getDescription());
                        String template = categoryVo.getSpec().getTemplate();
                        return viewNameResolver
                                .resolveViewNameOrDefault(request, template, DefaultTemplateEnum.CATEGORY.getValue())
                                .flatMap(viewName -> ServerResponse.ok().render(viewName, model));
                    })
                    .switchIfEmpty(Mono.error(new NotFoundException("Category not found with slug: " + slug)));
        };
    }

    Mono<CategoryVo> fetchBySlug(String slug) {
        return client.listBy(Category.class, listOptionsBySlug(slug), PageRequestImpl.ofSize(1))
                .mapNotNull(
                        result -> ListResult.first(result).map(CategoryVo::from).orElse(null));
    }

    private Mono<Boolean> categoryExistsBySlug(String slug) {
        return client.countBy(Category.class, listOptionsBySlug(slug)).map(count -> count > 0);
    }

    private ListOptions listOptionsBySlug(String slug) {
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(
                FieldSelector.of(and(equal("spec.slug", slug), isNull("metadata.deletionTimestamp"))));
        return listOptions;
    }

    private Mono<UrlContextListResult<ListedPostVo>> postListByCategoryName(String name, ServerRequest request) {
        String path = request.path();
        int pageNum = pageNumInPathVariable(request);
        return configuredPageSize(environmentFetcher, SystemSetting.Post::getCategoryPageSize)
                .flatMap(pageSize -> postFinder.listByCategory(pageNum, pageSize, name))
                .doOnNext(list -> list.forEach(postVo -> postVo.getSpec()
                        .setTitle(titleVisibilityIdentifyCalculator.calculateTitle(
                                postVo.getSpec().getTitle(),
                                postVo.getSpec().getVisible(),
                                localeContextResolver
                                        .resolveLocaleContext(request.exchange())
                                        .getLocale()))))
                .map(list -> new UrlContextListResult.Builder<ListedPostVo>()
                        .listResult(list)
                        .nextUrl(PageUrlUtils.nextPageUrl(path, totalPage(list)))
                        .prevUrl(PageUrlUtils.prevPageUrl(path))
                        .build());
    }
}
