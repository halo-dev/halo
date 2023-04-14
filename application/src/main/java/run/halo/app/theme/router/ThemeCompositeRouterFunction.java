package run.halo.app.theme.router;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SchemeInitializedEvent;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.router.factories.ArchiveRouteFactory;
import run.halo.app.theme.router.factories.AuthorPostsRouteFactory;
import run.halo.app.theme.router.factories.CategoriesRouteFactory;
import run.halo.app.theme.router.factories.CategoryPostRouteFactory;
import run.halo.app.theme.router.factories.IndexRouteFactory;
import run.halo.app.theme.router.factories.PostRouteFactory;
import run.halo.app.theme.router.factories.TagPostRouteFactory;
import run.halo.app.theme.router.factories.TagsRouteFactory;

/**
 * <p>The combination router of theme templates is used to render theme templates, but does not
 * include <code>page.html</code> templates which is processed separately.</p>
 *
 * @author guqing
 * @see SinglePageRoute
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
public class ThemeCompositeRouterFunction implements RouterFunction<ServerResponse> {
    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    private final ArchiveRouteFactory archiveRouteFactory;
    private final PostRouteFactory postRouteFactory;
    private final CategoriesRouteFactory categoriesRouteFactory;
    private final CategoryPostRouteFactory categoryPostRouteFactory;
    private final TagPostRouteFactory tagPostRouteFactory;
    private final TagsRouteFactory tagsRouteFactory;
    private final AuthorPostsRouteFactory authorPostsRouteFactory;
    private final IndexRouteFactory indexRouteFactory;

    private List<RouterFunction<ServerResponse>> cachedRouters = List.of();

    @Override
    @NonNull
    public Mono<HandlerFunction<ServerResponse>> route(@NonNull ServerRequest request) {
        return Flux.fromIterable(cachedRouters)
            .concatMap(routerFunction -> routerFunction.route(request))
            .next();
    }

    @Override
    public void accept(@NonNull RouterFunctions.Visitor visitor) {
        cachedRouters.forEach(routerFunction -> routerFunction.accept(visitor));
    }

    List<RouterFunction<ServerResponse>> routerFunctions() {
        return transformedPatterns()
            .stream()
            .map(this::createRouterFunction)
            .collect(Collectors.toList());
    }

    private RouterFunction<ServerResponse> createRouterFunction(RoutePattern routePattern) {
        return switch (routePattern.identifier()) {
            case POST -> postRouteFactory.create(routePattern.pattern());
            case ARCHIVES -> archiveRouteFactory.create(routePattern.pattern());
            case CATEGORIES -> categoriesRouteFactory.create(routePattern.pattern());
            case CATEGORY -> categoryPostRouteFactory.create(routePattern.pattern());
            case TAGS -> tagsRouteFactory.create(routePattern.pattern());
            case TAG -> tagPostRouteFactory.create(routePattern.pattern());
            case AUTHOR -> authorPostsRouteFactory.create(routePattern.pattern());
            case INDEX -> indexRouteFactory.create(routePattern.pattern());
            default ->
                throw new IllegalStateException("Unexpected value: " + routePattern.identifier());
        };
    }

    /**
     * Refresh the {@link #cachedRouters} when the permalink rule is changed.
     *
     * @param event {@link SchemeInitializedEvent} or {@link PermalinkRuleChangedEvent}
     */
    @EventListener({SchemeInitializedEvent.class, PermalinkRuleChangedEvent.class})
    public void onSchemeInitializedEvent(@NonNull ApplicationEvent event) {
        this.cachedRouters = routerFunctions();
    }

    record RoutePattern(DefaultTemplateEnum identifier, String pattern) {
    }

    private List<RoutePattern> transformedPatterns() {
        List<RoutePattern> routePatterns = new ArrayList<>();

        SystemSetting.ThemeRouteRules rules =
            environmentFetcher.fetch(SystemSetting.ThemeRouteRules.GROUP,
                    SystemSetting.ThemeRouteRules.class)
                .blockOptional()
                .orElse(SystemSetting.ThemeRouteRules.empty());
        String post = rules.getPost();
        routePatterns.add(new RoutePattern(DefaultTemplateEnum.POST, post));

        String archives = rules.getArchives();
        routePatterns.add(
            new RoutePattern(DefaultTemplateEnum.ARCHIVES, archives));

        String categories = rules.getCategories();
        routePatterns.add(
            new RoutePattern(DefaultTemplateEnum.CATEGORIES, categories));
        routePatterns.add(
            new RoutePattern(DefaultTemplateEnum.CATEGORY, categories));

        String tags = rules.getTags();
        routePatterns.add(new RoutePattern(DefaultTemplateEnum.TAGS, tags));
        routePatterns.add(new RoutePattern(DefaultTemplateEnum.TAG, tags));

        // Add the index route to the end to prevent conflict with the queryParam rule of the post
        routePatterns.add(new RoutePattern(DefaultTemplateEnum.INDEX, "/"));
        routePatterns.add(new RoutePattern(DefaultTemplateEnum.AUTHOR, ""));
        return routePatterns;
    }
}
