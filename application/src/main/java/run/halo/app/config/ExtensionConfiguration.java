package run.halo.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.SchemeWatcherManager;
import run.halo.app.extension.controller.ControllerManager;
import run.halo.app.extension.controller.DefaultControllerManager;
import run.halo.app.extension.router.ExtensionCompositeRouterFunction;
import run.halo.app.infra.properties.HaloProperties;

@Configuration(proxyBeanMethods = false)
public class ExtensionConfiguration {

    @Bean
    RouterFunction<ServerResponse> extensionsRouterFunction(ReactiveExtensionClient client,
        SchemeWatcherManager watcherManager, SchemeManager schemeManager) {
        return new ExtensionCompositeRouterFunction(client, watcherManager, schemeManager);
    }

    static class ExtensionControllerConfiguration {

        @Bean("controllerManager")
        ControllerManager controllerManager(ExtensionClient client, HaloProperties haloProperties) {
            return new DefaultControllerManager(client,
                haloProperties.getExtension().getController().isDisabled());
        }

    }

}
