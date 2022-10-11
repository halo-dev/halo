package run.halo.app.theme.router;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriUtils;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.router.strategy.DetailsPageRouteHandlerStrategy;
import run.halo.app.theme.router.strategy.IndexRouteStrategy;
import run.halo.app.theme.router.strategy.ListPageRouteHandlerStrategy;

/**
 * Permalink router for http get method.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class PermalinkHttpGetRouter implements InitializingBean {
    private static final ReentrantLock LOCK = new ReentrantLock();
    private final RadixRouterTree routeTree = new RadixRouterTree();
    private SystemConfigurableEnvironmentFetcher environmentFetcher;
    private ApplicationContext applicationContext;
    private HaloProperties haloProperties;

    /**
     * Match permalink according to {@link ServerRequest}.
     *
     * @param request http request
     * @return a handler function if matched, otherwise null
     */
    public HandlerFunction<ServerResponse> route(ServerRequest request) {
        MultiValueMap<String, String> queryParams = request.queryParams();
        String requestPath = request.path();
        // 文章的 permalink 规则需要对 p 参数规则特殊处理
        if (requestPath.equals("/") && queryParams.containsKey("p")) {
            // post special route path
            String postSlug = queryParams.getFirst("p");
            requestPath = requestPath + "?p=" + postSlug;
        }
        // /categories/{slug}/page/{page} 和 /tags/{slug}/page/{page} 需要去掉 page 部分
        if (PageUrlUtils.isPageUrl(requestPath)) {
            int i = requestPath.lastIndexOf("/page/");
            if (i != -1) {
                requestPath = requestPath.substring(0, i);
            }
        }
        return routeTree.match(requestPath);
    }

    public void insert(String key, HandlerFunction<ServerResponse> handlerFunction) {
        routeTree.insert(key, handlerFunction);
    }

    /**
     * Watch permalink changed event to refresh route tree.
     *
     * @param event permalink changed event
     */
    @EventListener(PermalinkIndexChangedEvent.class)
    public void onPermalinkChanged(PermalinkIndexChangedEvent event) {
        String oldPath = getPath(event.getOldPermalink());
        String path = getPath(event.getPermalink());
        GvkName gvkName = event.getGvkName();
        LOCK.lock();
        try {
            if (oldPath == null && path != null) {
                onPermalinkAdded(gvkName, path);
                return;
            }

            if (oldPath != null) {
                if (path == null) {
                    onPermalinkDeleted(oldPath);
                } else {
                    onPermalinkUpdated(gvkName, oldPath, path);
                }
            }
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Watch permalink rule changed event to refresh route tree for list style templates.
     *
     * @param event permalink changed event
     */
    @EventListener(PermalinkRuleChangedEvent.class)
    public void onPermalinkRuleChanged(PermalinkRuleChangedEvent event) {
        final String rule = event.getRule();
        final String oldRule = event.getOldRule();
        LOCK.lock();
        try {
            if (StringUtils.isNotBlank(oldRule)) {
                routeTree.delete(oldRule);
            }
            ListPageRouteHandlerStrategy routeStrategy = getRouteStrategy(event.getTemplate());
            if (routeStrategy == null) {
                return;
            }
            List<String> routerPaths = routeStrategy.getRouterPaths(oldRule);
            routeTreeBatchOperation(routerPaths, routeTree::delete);
            if (StringUtils.isNotBlank(rule)) {
                routeTreeBatchOperation(routeStrategy.getRouterPaths(rule),
                    path -> routeTree.insert(path, routeStrategy.getHandler()));
            }
        } finally {
            LOCK.unlock();
        }
    }

    void init() {
        // Index route need to be added first
        IndexRouteStrategy indexRouteStrategy =
            applicationContext.getBean(IndexRouteStrategy.class);
        List<String> routerPaths = indexRouteStrategy.getRouterPaths("/");
        routeTreeBatchOperation(routerPaths,
            path -> routeTree.insert(path, indexRouteStrategy.getHandler()));
    }

    private void routeTreeBatchOperation(List<String> paths,
        Consumer<String> templateFunction) {
        if (paths == null) {
            return;
        }
        paths.forEach(templateFunction);
    }

    private void onPermalinkAdded(GvkName gvkName, String path) {
        routeTree.insert(path, getRouteHandler(gvkName));
    }

    private void onPermalinkUpdated(GvkName gvkName, String oldPath, String path) {
        routeTree.delete(oldPath);
        routeTree.insert(path, getRouteHandler(gvkName));

    }

    private void onPermalinkDeleted(String path) {
        routeTree.delete(path);
    }

    private String getPath(@Nullable String permalink) {
        if (permalink == null) {
            return null;
        }
        String decode = UriUtils.decode(permalink, StandardCharsets.UTF_8);
        URI externalUrl = haloProperties.getExternalUrl();
        if (externalUrl != null) {
            String externalAsciiUrl = externalUrl.toASCIIString();
            return StringUtils.prependIfMissing(
                StringUtils.removeStart(decode, externalAsciiUrl), "/");
        }
        return decode;
    }

    private HandlerFunction<ServerResponse> getRouteHandler(GvkName gvkName) {
        GroupVersionKind gvk = gvkName.gvk();
        return applicationContext.getBeansOfType(DetailsPageRouteHandlerStrategy.class)
            .values()
            .stream()
            .filter(strategy -> strategy.supports(gvk))
            .findFirst()
            .map(strategy -> strategy.getHandler(getThemeRouteRules(), gvkName.name()))
            .orElse(null);
    }

    private ListPageRouteHandlerStrategy getRouteStrategy(DefaultTemplateEnum template) {
        return applicationContext.getBeansOfType(ListPageRouteHandlerStrategy.class)
            .values()
            .stream()
            .filter(strategy -> strategy.supports(template))
            .findFirst()
            .orElse(null);
    }

    public SystemSetting.ThemeRouteRules getThemeRouteRules() {
        return environmentFetcher.fetch(SystemSetting.ThemeRouteRules.GROUP,
            SystemSetting.ThemeRouteRules.class).block();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }
}
