package run.halo.app.plugin.extensionpoint;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ExtensionClient;

@Component
public class ExtensionDefinitionGetterImpl
    extends AbstractDefinitionGetter<ExtensionDefinition>
    implements ExtensionDefinitionGetter {

    public ExtensionDefinitionGetterImpl(ExtensionClient client) {
        super(client, new ExtensionDefinition());
    }

    @Override
    public Mono<ExtensionDefinition> get(String name) {
        return Mono.fromSupplier(() -> cache.get(name));
    }

    @Override
    void putCache(ExtensionDefinition definition) {
        cache.put(definition.getMetadata().getName(), definition);
    }
}
