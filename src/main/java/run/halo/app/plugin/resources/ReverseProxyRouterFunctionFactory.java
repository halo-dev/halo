package run.halo.app.plugin.resources;

import static org.springframework.http.MediaType.ALL;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.core.extension.ReverseProxy.FileReverseProxyProvider;
import run.halo.app.core.extension.ReverseProxy.ReverseProxyRule;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.plugin.PluginApplicationContext;
import run.halo.app.plugin.PluginConst;

/**
 * <p>Plugin's reverse proxy router factory.</p>
 * <p>It creates a {@link RouterFunction} based on the ReverseProxy rule configured by
 * the plugin.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
public class ReverseProxyRouterFunctionFactory {
    private static final String REVERSE_PROXY_API_PREFIX = "/assets";

    private final JsBundleRuleProvider jsBundleRuleProvider;

    public ReverseProxyRouterFunctionFactory(JsBundleRuleProvider jsBundleRuleProvider) {
        this.jsBundleRuleProvider = jsBundleRuleProvider;
    }

    /**
     * <p>Create {@link RouterFunction} according to the {@link ReverseProxy} custom resource
     * configuration of the plugin.</p>
     * <p>Note that: returns {@code Null} if the plugin does not have a {@link ReverseProxy} custom
     * resource.</p>
     *
     * @param applicationContext plugin application context or system application context
     * @return A reverse proxy RouterFunction handle(nullable)
     */
    @NonNull
    public Mono<RouterFunction<ServerResponse>> create(ReverseProxy reverseProxy,
        ApplicationContext applicationContext) {
        return createReverseProxyRouterFunction(reverseProxy, applicationContext);
    }

    private Mono<RouterFunction<ServerResponse>> createReverseProxyRouterFunction(
        ReverseProxy reverseProxy,
        ApplicationContext applicationContext) {
        Assert.notNull(reverseProxy, "The reverseProxy must not be null.");
        Assert.notNull(applicationContext, "The applicationContext must not be null.");
        final var pluginId = getPluginId(applicationContext);
        var rules = getReverseProxyRules(pluginId, reverseProxy);

        return rules.map(rule -> {
            String routePath = buildRoutePath(pluginId, rule);
            log.debug("Plugin [{}] registered reverse proxy route path [{}]", pluginId,
                routePath);
            return RouterFunctions.route(GET(routePath).and(accept(ALL)),
                request -> {
                    Resource resource =
                        loadResourceByFileRule(pluginId, applicationContext, rule, request);
                    if (!resource.exists()) {
                        return ServerResponse.notFound().build();
                    }
                    return ServerResponse.ok()
                        .bodyValue(resource);
                });
        }).reduce(RouterFunction::and);
    }

    private String getPluginId(ApplicationContext applicationContext) {
        if (applicationContext instanceof PluginApplicationContext pluginApplicationContext) {
            return pluginApplicationContext.getPluginId();
        }
        return PluginConst.SYSTEM_PLUGIN_NAME;
    }

    private Flux<ReverseProxyRule> getReverseProxyRules(String pluginId,
        ReverseProxy reverseProxy) {
        return Flux.fromIterable(reverseProxy.getRules())
            .concatWith(Flux.fromIterable(getJsBundleRules(pluginId)));
    }

    private List<ReverseProxyRule> getJsBundleRules(String pluginId) {
        List<ReverseProxyRule> rules = new ArrayList<>(2);
        jsBundleRuleProvider.jsRule(pluginId).ifPresent(rules::add);
        jsBundleRuleProvider.cssRule(pluginId).ifPresent(rules::add);
        return rules;
    }

    public static String buildRoutePath(String pluginId, ReverseProxyRule reverseProxyRule) {
        return PathUtils.combinePath(REVERSE_PROXY_API_PREFIX, pluginId, reverseProxyRule.path());
    }

    /**
     * <p>File load rule: if the directory is configured but the file name is not configured, it
     * means access through wildcards. Otherwise, if only the file name is configured, this
     * method only returns the file pointed to by the rule.</p>
     * <p>You should only use {@link Resource#getInputStream()} to get resource content instead of
     * {@link Resource#getFile()},the resource is loaded from the plugin jar file using a
     * specific plugin class loader; if you use {@link Resource#getFile()}, you cannot get the
     * file.</p>
     * <p>Note that a returned Resource handle does not imply an existing resource; you need to
     * invoke {@link Resource#exists()} to check for existence</p>
     *
     * @param pluginApplicationContext load file from plugin
     * @param rule reverse proxy rule
     * @param request client request
     * @return a Resource handle for the specified resource location by the plugin(never null);
     */
    @NonNull
    private Resource loadResourceByFileRule(String pluginId,
        ApplicationContext pluginApplicationContext,
        ReverseProxyRule rule, ServerRequest request) {
        Assert.notNull(rule.file(), "File rule must not be null.");
        FileReverseProxyProvider file = rule.file();
        String directory = file.directory();

        // Decision file name
        String filename;
        String configuredFilename = file.filename();
        if (StringUtils.isNotBlank(configuredFilename)) {
            filename = configuredFilename;
        } else {
            AntPathMatcher antPathMatcher = new AntPathMatcher();
            String routePath = buildRoutePath(pluginId, rule);
            filename =
                antPathMatcher.extractPathWithinPattern(routePath, request.path());
        }

        String filePath = PathUtils.appendPathSeparatorIfMissing(directory) + filename;
        return pluginApplicationContext.getResource(filePath);
    }
}

