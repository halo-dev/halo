package run.halo.app.theme.router;

import static run.halo.app.theme.utils.PatternUtils.normalizePattern;
import static run.halo.app.theme.utils.PatternUtils.normalizePostPattern;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.SmartLifecycle;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigChangedEvent;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.SystemSetting.ThemeRouteRules;
import run.halo.app.infra.utils.ReactiveUtils;
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
@Slf4j
@Component
@RequiredArgsConstructor
public class ThemeCompositeRouterFunction implements
    RouterFunction<ServerResponse>,
    SmartLifecycle,
    ApplicationListener<SystemConfigChangedEvent> {

    private static final Duration BLOCKING_TIMEOUT = ReactiveUtils.DEFAULT_TIMEOUT;

    private final SystemConfigFetcher environmentFetcher;

    private final ArchiveRouteFactory archiveRouteFactory;
    private final PostRouteFactory postRouteFactory;
    private final CategoriesRouteFactory categoriesRouteFactory;
    private final CategoryPostRouteFactory categoryPostRouteFactory;
    private final TagPostRouteFactory tagPostRouteFactory;
    private final TagsRouteFactory tagsRouteFactory;
    private final AuthorPostsRouteFactory authorPostsRouteFactory;
    private final IndexRouteFactory indexRouteFactory;

    private List<RouterFunction<ServerResponse>> cachedRouters = List.of();
    private volatile boolean running;

    @Override
    public void onApplicationEvent(SystemConfigChangedEvent event) {
        var oldData = event.getOldData();
        var newData = event.getNewData();
        var oldRules = SystemSetting.get(oldData, ThemeRouteRules.GROUP,
            ThemeRouteRules.class);
        if (oldRules == null) {
            oldRules = ThemeRouteRules.empty();
        }
        var newRules = SystemSetting.get(newData, ThemeRouteRules.GROUP,
            ThemeRouteRules.class);
        if (newRules == null) {
            newRules = ThemeRouteRules.empty();
        }
        boolean rulesChanged = !Objects.equals(oldRules, newRules);
        if (rulesChanged) {
            log.info("Theme route rules changed, updating router functions...");
            if (log.isDebugEnabled()) {
                log.debug("Old theme route rules: {}", oldRules);
                log.debug("New theme route rules: {}", newRules);
            }
            this.cachedRouters = routerFunctions(newRules);
            log.info("Theme route rules updated.");
        }
    }

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

    private List<RouterFunction<ServerResponse>> routerFunctions(ThemeRouteRules rules) {
        return transformedPatterns(rules).stream()
            .map(this::createRouterFunction)
            .toList();
    }

    private List<RouterFunction<ServerResponse>> routerFunctions() {
        return transformedPatterns().stream()
            .map(this::createRouterFunction)
            .toList();
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

    @Override
    public void start() {
        if (running) {
            return;
        }
        running = true;
        this.cachedRouters = routerFunctions();
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        this.cachedRouters = List.of();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    record RoutePattern(DefaultTemplateEnum identifier, String pattern) {
    }

    private List<RoutePattern> transformedPatterns(ThemeRouteRules rules) {
        List<RoutePattern> routePatterns = new ArrayList<>();
        var archives = normalizePattern(rules.getArchives());
        routePatterns.add(new RoutePattern(DefaultTemplateEnum.ARCHIVES, archives));

        var categories = normalizePattern(rules.getCategories());
        routePatterns.add(new RoutePattern(DefaultTemplateEnum.CATEGORIES, categories));
        routePatterns.add(new RoutePattern(DefaultTemplateEnum.CATEGORY, categories));

        var tags = normalizePattern(rules.getTags());
        routePatterns.add(new RoutePattern(DefaultTemplateEnum.TAGS, tags));
        routePatterns.add(new RoutePattern(DefaultTemplateEnum.TAG, tags));

        var post = normalizePostPattern(rules);
        routePatterns.add(new RoutePattern(DefaultTemplateEnum.POST, post));

        // Add the index route to the end to prevent conflict with the queryParam rule of the post
        routePatterns.add(new RoutePattern(DefaultTemplateEnum.AUTHOR, ""));
        routePatterns.add(new RoutePattern(DefaultTemplateEnum.INDEX, "/"));
        return routePatterns;

    }

    private List<RoutePattern> transformedPatterns() {
        var rules = environmentFetcher.fetch(ThemeRouteRules.GROUP, ThemeRouteRules.class)
            .blockOptional(BLOCKING_TIMEOUT)
            .orElseGet(ThemeRouteRules::empty);
        return transformedPatterns(rules);
    }

}
