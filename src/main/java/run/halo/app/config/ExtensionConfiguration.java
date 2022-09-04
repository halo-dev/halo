package run.halo.app.config;

import org.pf4j.PluginManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.content.ContentService;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.Theme;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.reconciler.MenuItemReconciler;
import run.halo.app.core.extension.reconciler.MenuReconciler;
import run.halo.app.core.extension.reconciler.PluginReconciler;
import run.halo.app.core.extension.reconciler.PostReconciler;
import run.halo.app.core.extension.reconciler.RoleBindingReconciler;
import run.halo.app.core.extension.reconciler.RoleReconciler;
import run.halo.app.core.extension.reconciler.ThemeReconciler;
import run.halo.app.core.extension.reconciler.UserReconciler;
import run.halo.app.core.extension.reconciler.attachment.AttachmentReconciler;
import run.halo.app.core.extension.service.RoleService;
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
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.plugin.HaloPluginManager;
import run.halo.app.plugin.resources.JsBundleRuleProvider;

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
        Controller pluginController(ExtensionClient client, HaloPluginManager haloPluginManager,
            JsBundleRuleProvider jsBundleRule) {
            return new ControllerBuilder("plugin-controller", client)
                .reconciler(new PluginReconciler(client, haloPluginManager, jsBundleRule))
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
        Controller postController(ExtensionClient client, ContentService contentService) {
            return new ControllerBuilder("post-controller", client)
                .reconciler(new PostReconciler(client, contentService))
                .extension(new Post())
                .build();
        }

        @Bean
        Controller attachmentController(ExtensionClient client, PluginManager pluginManager) {
            return new ControllerBuilder("attachment-controller", client)
                .reconciler(new AttachmentReconciler(client, pluginManager))
                .extension(new Attachment())
                .build();
        }
    }

}
