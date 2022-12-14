package run.halo.app.core.extension.reconciler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.plugin.PluginConst;
import run.halo.app.plugin.resources.ReverseProxyRouterFunctionRegistry;

/**
 * Reconciler for {@link ReverseProxy}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class ReverseProxyReconciler implements Reconciler<Reconciler.Request> {
    private static final String FINALIZER_NAME = "reverse-proxy-protection";
    private final ExtensionClient client;
    private final ReverseProxyRouterFunctionRegistry routerFunctionRegistry;

    public ReverseProxyReconciler(ExtensionClient client,
        ReverseProxyRouterFunctionRegistry routerFunctionRegistry) {
        this.client = client;
        this.routerFunctionRegistry = routerFunctionRegistry;
    }

    @Override
    public Result reconcile(Request request) {
        return client.fetch(ReverseProxy.class, request.name())
            .map(reverseProxy -> {
                if (isDeleted(reverseProxy)) {
                    cleanUpResourcesAndRemoveFinalizer(request.name());
                    return new Result(false, null);
                }
                addFinalizerIfNecessary(reverseProxy);
                registerReverseProxy(reverseProxy);
                return new Result(false, null);
            })
            .orElse(new Result(false, null));
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new ReverseProxy())
            .build();
    }

    private void registerReverseProxy(ReverseProxy reverseProxy) {
        String pluginId = getPluginId(reverseProxy);
        routerFunctionRegistry.register(pluginId, reverseProxy).block();
    }

    private void cleanUpResources(ReverseProxy reverseProxy) {
        String pluginId = getPluginId(reverseProxy);
        routerFunctionRegistry.remove(pluginId, reverseProxy.getMetadata().getName()).block();
    }

    private void addFinalizerIfNecessary(ReverseProxy oldReverseProxy) {
        Set<String> finalizers = oldReverseProxy.getMetadata().getFinalizers();
        if (finalizers != null && finalizers.contains(FINALIZER_NAME)) {
            return;
        }
        client.fetch(ReverseProxy.class, oldReverseProxy.getMetadata().getName())
            .ifPresent(reverseProxy -> {
                Set<String> newFinalizers = reverseProxy.getMetadata().getFinalizers();
                if (newFinalizers == null) {
                    newFinalizers = new HashSet<>();
                    reverseProxy.getMetadata().setFinalizers(newFinalizers);
                }
                newFinalizers.add(FINALIZER_NAME);
                client.update(reverseProxy);
            });
    }

    private void cleanUpResourcesAndRemoveFinalizer(String name) {
        client.fetch(ReverseProxy.class, name).ifPresent(reverseProxy -> {
            cleanUpResources(reverseProxy);
            if (reverseProxy.getMetadata().getFinalizers() != null) {
                reverseProxy.getMetadata().getFinalizers().remove(FINALIZER_NAME);
            }
            client.update(reverseProxy);
        });
    }

    private boolean isDeleted(ReverseProxy reverseProxy) {
        return reverseProxy.getMetadata().getDeletionTimestamp() != null;
    }

    private String getPluginId(ReverseProxy reverseProxy) {
        Map<String, String> labels = reverseProxy.getMetadata().getLabels();
        if (labels == null) {
            return PluginConst.SYSTEM_PLUGIN_NAME;
        }
        return StringUtils.defaultString(labels.get(PluginConst.PLUGIN_NAME_LABEL_NAME),
            PluginConst.SYSTEM_PLUGIN_NAME);
    }
}
