package run.halo.app.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.extension.DefaultSchemeManager;
import run.halo.app.extension.DefaultSchemeWatcherManager;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.SchemeWatcherManager;
import run.halo.app.extension.controller.DefaultControllerManager;
import run.halo.app.extension.router.ExtensionCompositeRouterFunction;

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
        DefaultControllerManager controllerManager(ExtensionClient client) {
            return new DefaultControllerManager(client);
        }

    }

}
