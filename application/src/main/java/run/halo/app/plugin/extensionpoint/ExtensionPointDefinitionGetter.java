package run.halo.app.plugin.extensionpoint;

import reactor.core.publisher.Mono;

public interface ExtensionPointDefinitionGetter {

    /**
     * Gets extension point definition by extension point class.
     * <p>Retrieve by filedSelector: <code>spec.className</code></p>
     *
     * @param className extension point class name
     */
    Mono<ExtensionPointDefinition> getByClassName(String className);
}
