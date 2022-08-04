package run.halo.app.config;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.reconciler.MenuItemReconciler;
import run.halo.app.core.extension.reconciler.MenuReconciler;
import run.halo.app.core.extension.reconciler.PluginReconciler;
import run.halo.app.core.extension.reconciler.RoleBindingReconciler;
import run.halo.app.core.extension.reconciler.RoleReconciler;
import run.halo.app.core.extension.reconciler.UserReconciler;
import run.halo.app.core.extension.service.RoleService;
import run.halo.app.extension.DefaultExtensionClient;
import run.halo.app.extension.DefaultSchemeManager;
import run.halo.app.extension.DefaultSchemeWatcherManager;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.JSONExtensionConverter;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.SchemeWatcherManager;
import run.halo.app.extension.SchemeWatcherManager.SchemeWatcher;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.ControllerManager;
import run.halo.app.extension.router.ExtensionCompositeRouterFunction;
import run.halo.app.extension.store.ExtensionStoreClient;
import run.halo.app.plugin.HaloPluginManager;
import run.halo.app.plugin.resources.JsBundleRuleProvider;

@Configuration(proxyBeanMethods = false)
public class ExtensionConfiguration {

    @Bean
    RouterFunction<ServerResponse> extensionsRouterFunction(ExtensionClient client,
        SchemeWatcherManager watcherManager) {
        return new ExtensionCompositeRouterFunction(client, watcherManager);
    }

    @Bean
    ExtensionClient extensionClient(ExtensionStoreClient storeClient, SchemeManager schemeManager) {
        var converter = new JSONExtensionConverter(schemeManager);
        return new DefaultExtensionClient(storeClient, converter, schemeManager);
    }

    @Bean
    SchemeManager schemeManager(SchemeWatcherManager watcherManager, List<SchemeWatcher> watchers) {
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
    }

}
