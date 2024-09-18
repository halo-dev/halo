package run.halo.app.plugin.extensionpoint;

import java.util.List;
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
     * Get the extension(s) according to the {@code ExtensionPointDefinition} queried
     * by incoming extension point class.
     *
     * @param extensionPoint extension point class
     * @return implementations of the corresponding extension point.
     * @throws IllegalArgumentException if the incoming extension point class does not have
     *                                  the {@code ExtensionPointDefinition}.
     */
    <T extends ExtensionPoint> Flux<T> getEnabledExtensions(Class<T> extensionPoint);

    /**
     * Get all extensions according to extension point class.
     *
     * @param extensionPointClass extension point class
     * @param <T> type of extension point
     * @return a bunch of extension points.
     */
    <T extends ExtensionPoint> Flux<T> getExtensions(Class<T> extensionPointClass);

    /**
     * Get all extensions according to extension point class.
     *
     * @param extensionPointClass extension point class
     * @param <T> type of extension point
     * @return a bunch of extension points.
     */
    <T extends ExtensionPoint> List<T> getExtensionList(Class<T> extensionPointClass);

}
