package run.halo.app.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.content.ContentService;
import run.halo.app.content.permalinks.CategoryPermalinkPolicy;
import run.halo.app.content.permalinks.PostPermalinkPolicy;
import run.halo.app.content.permalinks.TagPermalinkPolicy;
import run.halo.app.core.extension.Category;
import run.halo.app.core.extension.Comment;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.SinglePage;
import run.halo.app.core.extension.Tag;
import run.halo.app.core.extension.Theme;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.reconciler.CategoryReconciler;
import run.halo.app.core.extension.reconciler.CommentReconciler;
import run.halo.app.core.extension.reconciler.MenuItemReconciler;
import run.halo.app.core.extension.reconciler.MenuReconciler;
import run.halo.app.core.extension.reconciler.PluginReconciler;
import run.halo.app.core.extension.reconciler.PostReconciler;
import run.halo.app.core.extension.reconciler.ReverseProxyReconciler;
import run.halo.app.core.extension.reconciler.RoleBindingReconciler;
import run.halo.app.core.extension.reconciler.RoleReconciler;
import run.halo.app.core.extension.reconciler.SinglePageReconciler;
import run.halo.app.core.extension.reconciler.SystemSettingReconciler;
import run.halo.app.core.extension.reconciler.TagReconciler;
import run.halo.app.core.extension.reconciler.ThemeReconciler;
import run.halo.app.core.extension.reconciler.UserReconciler;
import run.halo.app.core.extension.reconciler.attachment.AttachmentReconciler;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.DefaultSchemeManager;
import run.halo.app.extension.DefaultSchemeWatcherManager;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.SchemeWatcherManager;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.ControllerManager;
import run.halo.app.extension.router.ExtensionCompositeRouterFunction;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.plugin.ExtensionComponentsFinder;
import run.halo.app.plugin.HaloPluginManager;
import run.halo.app.plugin.resources.ReverseProxyRouterFunctionRegistry;

@Configuration(proxyBeanMethods = false)
public class ExtensionConfiguration {

    @Bean
    RouterFunction<ServerResponse> extensionsRouterFunction(ReactiveExtensionClient client,
        SchemeWatcherManager watcherManager) {
        return new ExtensionCompositeRouterFunction(client, watcherManager);
    }

    @Bean
    SchemeManager schemeManager(SchemeWatcherManager watcherManager) {
        return new DefaultSchemeManager(watcherManager);
    }

    @Bean
    SchemeWatcherManager schemeWatcherManager() {
        return new DefaultSchemeWatcherManager();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "halo.extension.controller.disabled",
        havingValue = "false",
        matchIfMissing = true)
    static class ExtensionControllerConfiguration {

        @Bean
        ControllerManager controllerManager() {
            return new ControllerManager();
        }

        @Bean
        Controller userController(ExtensionClient client) {
            return new ControllerBuilder("user-controller", client)
                .reconciler(new UserReconciler(client))
                .extension(new User())
                .build();
        }

        @Bean
        Controller roleController(ExtensionClient client, RoleService roleService) {
            return new ControllerBuilder("role-controller", client)
                .reconciler(new RoleReconciler(client, roleService))
                .extension(new Role())
                .build();
        }

        @Bean
        Controller roleBindingController(ExtensionClient client) {
            return new ControllerBuilder("role-binding-controller", client)
                .reconciler(new RoleBindingReconciler(client))
                .extension(new RoleBinding())
                .build();
        }

        @Bean
        Controller pluginController(ExtensionClient client, HaloPluginManager haloPluginManager) {
            return new ControllerBuilder("plugin-controller", client)
                .reconciler(new PluginReconciler(client, haloPluginManager))
                .extension(new Plugin())
                .build();
        }

        @Bean
        Controller menuController(ExtensionClient client) {
            return new ControllerBuilder("menu-controller", client)
                .reconciler(new MenuReconciler(client))
                .extension(new Menu())
                .build();
        }

        @Bean
        Controller menuItemController(ExtensionClient client) {
            return new ControllerBuilder("menu-item-controller", client)
                .reconciler(new MenuItemReconciler(client))
                .extension(new MenuItem())
                .build();
        }

        @Bean
        Controller themeController(ExtensionClient client, HaloProperties haloProperties) {
            return new ControllerBuilder("theme-controller", client)
                .reconciler(new ThemeReconciler(client, haloProperties))
                .extension(new Theme())
                .build();
        }

        @Bean
        Controller postController(ExtensionClient client, ContentService contentService,
            PostPermalinkPolicy postPermalinkPolicy) {
            return new ControllerBuilder("post-controller", client)
                .reconciler(new PostReconciler(client, contentService, postPermalinkPolicy))
                .extension(new Post())
                .build();
        }

        @Bean
        Controller categoryController(ExtensionClient client,
            CategoryPermalinkPolicy categoryPermalinkPolicy) {
            return new ControllerBuilder("category-controller", client)
                .reconciler(new CategoryReconciler(client, categoryPermalinkPolicy))
                .extension(new Category())
                .build();
        }

        @Bean
        Controller tagController(ExtensionClient client, TagPermalinkPolicy tagPermalinkPolicy) {
            return new ControllerBuilder("tag-controller", client)
                .reconciler(new TagReconciler(client, tagPermalinkPolicy))
                .extension(new Tag())
                .build();
        }

        @Bean
        Controller systemSettingController(ExtensionClient client,
            SystemConfigurableEnvironmentFetcher environmentFetcher,
            ApplicationContext applicationContext) {
            return new ControllerBuilder("system-setting-controller", client)
                .reconciler(new SystemSettingReconciler(client, environmentFetcher,
                    applicationContext))
                .extension(new ConfigMap())
                .build();
        }

        @Bean
        Controller attachmentController(ExtensionClient client,
            ExtensionComponentsFinder extensionComponentsFinder,
            ExternalUrlSupplier externalUrl) {
            return new ControllerBuilder("attachment-controller", client)
                .reconciler(
                    new AttachmentReconciler(client, extensionComponentsFinder, externalUrl))
                .extension(new Attachment())
                .build();
        }

        @Bean
        Controller singlePageController(ExtensionClient client, ContentService contentService,
            ApplicationContext applicationContext) {
            return new ControllerBuilder("single-page-controller", client)
                .reconciler(new SinglePageReconciler(client, contentService,
                    applicationContext)
                )
                .extension(new SinglePage())
                .build();
        }

        @Bean
        Controller commentController(ExtensionClient client, MeterRegistry meterRegistry,
            SchemeManager schemeManager) {
            return new ControllerBuilder("comment-controller", client)
                .reconciler(new CommentReconciler(client, meterRegistry, schemeManager))
                .extension(new Comment())
                .build();
        }

        @Bean
        Controller reverseProxyController(ExtensionClient client,
            ReverseProxyRouterFunctionRegistry reverseProxyRouterFunctionRegistry) {
            return new ControllerBuilder("reverse-proxy-controller", client)
                .reconciler(new ReverseProxyReconciler(client, reverseProxyRouterFunctionRegistry))
                .extension(new ReverseProxy())
                .build();
        }
    }

}
