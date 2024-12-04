package run.halo.app.plugin.extensionpoint;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ExtensionClient;

@Component
public class ExtensionPointDefinitionGetterImpl
    extends AbstractDefinitionGetter<ExtensionPointDefinition>
    implements ExtensionPointDefinitionGetter {

    public ExtensionPointDefinitionGetterImpl(ExtensionClient client) {
        super(client, new ExtensionPointDefinition());
    }

    @Override
    public Mono<ExtensionPointDefinition> getByClassName(String className) {
        return Mono.fromSupplier(() -> cache.get(className));
    }

    @Override
    void putCache(ExtensionPointDefinition definition) {
        var className = definition.getSpec().getClassName();
        cache.put(className, definition);
    }
}
