package run.halo.app.plugin.extensionpoint;

import reactor.core.publisher.Mono;

public interface ExtensionDefinitionGetter {

    /**
     * Gets extension definition by extension definition name.
     *
     * @param name extension definition name
     */
    Mono<ExtensionDefinition> get(String name);
}
