package run.halo.app.infra.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.DefaultControllerManager;
import run.halo.app.extension.index.IndexSnapshotManager;
import run.halo.app.infra.properties.HaloProperties;

@Configuration(proxyBeanMethods = false)
public class ExtensionConfiguration {

    @Bean
    @ConditionalOnProperty(name = "halo.extension.controller.disabled", havingValue = "false", matchIfMissing = true)
    DefaultControllerManager controllerManager(ExtensionClient client) {
        return new DefaultControllerManager(client);
    }

    @Bean
    IndexSnapshotManager indexSnapshotManager(HaloProperties haloProperties) {
        return new IndexSnapshotManager(haloProperties);
    }
}
