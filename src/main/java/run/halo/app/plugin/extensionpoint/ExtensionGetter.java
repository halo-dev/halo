package run.halo.app.plugin.extensionpoint;

import org.pf4j.ExtensionPoint;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExtensionGetter {

    /**
     * Get only one enabled extension from system configuration.
     *
     * @param extensionPoint is extension point class.
     * @return implementation of the corresponding extension point. If no configuration is found,
     * we will use the default implementation from application context instead.
     */
    <T extends ExtensionPoint> Mono<T> getEnabledExtension(Class<T> extensionPoint);

    /**
     * Get enabled extension list from system configuration.
     *
     * @param extensionPoint is extension point class.
     * @return implementations of the corresponding extension point. If no configuration is found,
     * we will use the default implementation from application context instead.
     */
    <T extends ExtensionPoint> Flux<T> getEnabledExtensions(Class<T> extensionPoint);

}
