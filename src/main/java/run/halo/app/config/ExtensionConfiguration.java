package run.halo.app.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.extension.DefaultExtensionClient;
import run.halo.app.extension.DefaultSchemeManager;
import run.halo.app.extension.DefaultSchemeWatcherManager;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionCompositeRouterFunction;
import run.halo.app.extension.JSONExtensionConverter;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.SchemeWatcherManager;
import run.halo.app.extension.SchemeWatcherManager.SchemeWatcher;
import run.halo.app.extension.store.ExtensionStoreClient;

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
}
