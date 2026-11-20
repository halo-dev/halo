package run.halo.app.plugin;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.util.function.Consumer;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import run.halo.app.content.PostContentService;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.DefaultSchemeManager;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.BackupRootGetter;
import run.halo.app.infra.ExternalLinkProcessor;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.SystemInfoGetter;
import run.halo.app.notification.NotificationCenter;
import run.halo.app.notification.NotificationReasonEmitter;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.security.LoginHandlerEnhancer;
import run.halo.app.security.authentication.CryptoService;

/**
 * Utility for creating shared application context.
 *
 * @author guqing
 * @author johnniang
 * @since 2.12.0
 */
public enum SharedApplicationContextFactory {
    ;

    public static ApplicationContext create(ApplicationContext rootContext) {
        // TODO Optimize creation timing
        var sharedContext = new GenericApplicationContext();
        // Closed by the root plugin manager after all plugin contexts have stopped.

        var beanFactory = sharedContext.getBeanFactory();

        // register shared object here
        var extensionClient = rootContext.getBean(ExtensionClient.class);
        var reactiveExtensionClient = rootContext.getBean(ReactiveExtensionClient.class);
        beanFactory.registerSingleton("extensionClient", extensionClient);
        beanFactory.registerSingleton("reactiveExtensionClient", reactiveExtensionClient);
        beanFactory.registerSingleton(
                "reactivePresentationExtensionClient",
                rootContext.getBean(run.halo.app.extension.ReactivePresentationExtensionClient.class));
        beanFactory.registerSingleton(
                "resourcePresentationService",
                rootContext.getBean(run.halo.app.localization.ResourcePresentationService.class));
        beanFactory.registerSingleton(
                "resourceLocalizationDescriptors",
                rootContext.getBean(run.halo.app.localization.ResourceLocalizationDescriptors.class));
        beanFactory.registerSingleton(
                "resourceLocalizationDescriptorLocalizer",
                rootContext.getBean(run.halo.app.localization.ResourceLocalizationDescriptorLocalizer.class));
        beanFactory.registerSingleton(
                "publicApiLocalizationFilter",
                rootContext.getBean(run.halo.app.localization.PublicApiLocalizationFilter.class));

        DefaultSchemeManager defaultSchemeManager = rootContext.getBean(DefaultSchemeManager.class);
        beanFactory.registerSingleton("schemeManager", defaultSchemeManager);
        beanFactory.registerSingleton(
                "resourceLocalizationService",
                rootContext.getBean(run.halo.app.localization.ResourceLocalizationService.class));
        beanFactory.registerSingleton("externalUrlSupplier", rootContext.getBean(ExternalUrlSupplier.class));
        beanFactory.registerSingleton(
                "serverSecurityContextRepository", rootContext.getBean(ServerSecurityContextRepository.class));
        beanFactory.registerSingleton("attachmentService", rootContext.getBean(AttachmentService.class));
        beanFactory.registerSingleton("backupRootGetter", rootContext.getBean(BackupRootGetter.class));
        beanFactory.registerSingleton(
                "notificationReasonEmitter", rootContext.getBean(NotificationReasonEmitter.class));
        beanFactory.registerSingleton("notificationCenter", rootContext.getBean(NotificationCenter.class));
        beanFactory.registerSingleton("externalLinkProcessor", rootContext.getBean(ExternalLinkProcessor.class));
        beanFactory.registerSingleton("postContentService", rootContext.getBean(PostContentService.class));
        beanFactory.registerSingleton("cacheManager", rootContext.getBean(CacheManager.class));
        beanFactory.registerSingleton("loginHandlerEnhancer", rootContext.getBean(LoginHandlerEnhancer.class));
        rootContext
                .getBeanProvider(PluginsRootGetter.class)
                .ifUnique(pluginsRootGetter -> beanFactory.registerSingleton("pluginsRootGetter", pluginsRootGetter));
        beanFactory.registerSingleton("extensionGetter", rootContext.getBean(ExtensionGetter.class));
        rootContext
                .getBeanProvider(CryptoService.class)
                .ifUnique(cryptoService -> beanFactory.registerSingleton("cryptoService", cryptoService));
        rootContext
                .getBeanProvider(RateLimiterRegistry.class)
                .ifUnique(rateLimiterRegistry ->
                        beanFactory.registerSingleton("rateLimiterRegistry", rateLimiterRegistry));

        // Authentication plugins may need this RequestCache to handle successful login redirect
        rootContext
                .getBeanProvider(ServerRequestCache.class)
                .ifUnique(
                        serverRequestCache -> beanFactory.registerSingleton("serverRequestCache", serverRequestCache));
        rootContext
                .getBeanProvider(UserService.class)
                .ifUnique(userService -> beanFactory.registerSingleton("userService", userService));
        rootContext
                .getBeanProvider(RoleService.class)
                .ifUnique(roleService -> beanFactory.registerSingleton("roleService", roleService));
        rootContext
                .getBeanProvider(ReactiveUserDetailsService.class)
                .ifUnique(
                        userDetailsService -> beanFactory.registerSingleton("userDetailsService", userDetailsService));
        rootContext
                .getBeanProvider(SystemInfoGetter.class)
                .ifUnique(systemInfoGetter -> beanFactory.registerSingleton("systemInfoGetter", systemInfoGetter));
        rootContext.getBeanProvider(PluginRouterFunctionRegistry.class).ifUnique(registry -> {
            if (registry instanceof RouterFunction<?> routes) {
                // Read-only traversal must not be discovered as another RouterFunction in plugin contexts.
                Consumer<RouterFunctions.Visitor> visitor = routes::accept;
                beanFactory.registerSingleton("pluginRouteVisitor", visitor);
            }
        });
        // TODO add more shared instance here

        sharedContext.refresh();
        return sharedContext;
    }
}
