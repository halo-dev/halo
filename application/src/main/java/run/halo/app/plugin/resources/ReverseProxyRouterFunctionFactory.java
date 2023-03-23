package run.halo.app.plugin.resources;

import static org.springframework.http.MediaType.ALL;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.server.PathContainer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.core.extension.ReverseProxy.FileReverseProxyProvider;
import run.halo.app.core.extension.ReverseProxy.ReverseProxyRule;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.plugin.ExtensionContextRegistry;
import run.halo.app.plugin.HaloPluginManager;
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
@AllArgsConstructor
public class ReverseProxyRouterFunctionFactory {

    private final HaloPluginManager haloPluginManager;
    private final ApplicationContext applicationContext;

    /**
     * <p>Create {@link RouterFunction} according to the {@link ReverseProxy} custom resource
     * configuration of the plugin.</p>
     * <p>Note that: returns {@code Null} if the plugin does not have a {@link ReverseProxy} custom
     * resource.</p>
     *
     * @param pluginName plugin name(nullable if system)
     * @return A reverse proxy RouterFunction handle(nullable)
     */
    @NonNull
    public Mono<RouterFunction<ServerResponse>> create(ReverseProxy reverseProxy,
        String pluginName) {
        return createReverseProxyRouterFunction(reverseProxy, nullSafePluginName(pluginName));
    }

    private Mono<RouterFunction<ServerResponse>> createReverseProxyRouterFunction(
        ReverseProxy reverseProxy, @NonNull String pluginName) {
        Assert.notNull(reverseProxy, "The reverseProxy must not be null.");
        var rules = getReverseProxyRules(reverseProxy);

        return rules.map(rule -> {
            String routePath = buildRoutePath(pluginName, rule);
            log.debug("Plugin [{}] registered reverse proxy route path [{}]", pluginName,
                routePath);
            return RouterFunctions.route(GET(routePath).and(accept(ALL)),
                request -> {
                    Resource resource =
                        loadResourceByFileRule(pluginName, rule, request);
                    if (!resource.exists()) {
                        return ServerResponse.notFound().build();
                    }
                    return ServerResponse.ok()
                        .bodyValue(resource);
                });
        }).reduce(RouterFunction::and);
    }

    private String nullSafePluginName(String pluginName) {
        return pluginName == null ? PluginConst.SYSTEM_PLUGIN_NAME : pluginName;
    }

    private Flux<ReverseProxyRule> getReverseProxyRules(ReverseProxy reverseProxy) {
        return Flux.fromIterable(reverseProxy.getRules());
    }

    public static String buildRoutePath(String pluginId, ReverseProxyRule reverseProxyRule) {
        return PathUtils.combinePath(PluginConst.assertsRoutePrefix(pluginId),
            reverseProxyRule.path());
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
     * @param pluginName plugin to load file by name
     * @param rule reverse proxy rule
     * @param request client request
     * @return a Resource handle for the specified resource location by the plugin(never null);
     */
    @NonNull
    private Resource loadResourceByFileRule(String pluginName, ReverseProxyRule rule,
        ServerRequest request) {
        Assert.notNull(rule.file(), "File rule must not be null.");
        FileReverseProxyProvider file = rule.file();
        String directory = file.directory();

        // Decision file name
        String filename;
        String configuredFilename = file.filename();
        if (StringUtils.isNotBlank(configuredFilename)) {
            filename = configuredFilename;
        } else {
            String routePath = buildRoutePath(pluginName, rule);
            PathContainer pathContainer = PathPatternParser.defaultInstance.parse(routePath)
                .extractPathWithinPattern(PathContainer.parsePath(request.path()));
            filename = pathContainer.value();
        }

        String filePath = PathUtils.combinePath(directory, filename);
        return getResourceLoader(pluginName).getResource(filePath);
    }

    private ResourceLoader getResourceLoader(String pluginName) {
        ExtensionContextRegistry registry = ExtensionContextRegistry.getInstance();
        if (registry.containsContext(pluginName)) {
            return registry.getByPluginId(pluginName);
        }
        if (PluginConst.SYSTEM_PLUGIN_NAME.equals(pluginName)) {
            return applicationContext;
        }
        DefaultResourceLoader resourceLoader =
            BundleResourceUtils.getResourceLoader(haloPluginManager, pluginName);
        if (resourceLoader == null) {
            throw new NotFoundException("Plugin [" + pluginName + "] not found.");
        }
        return resourceLoader;
    }
}

