package run.halo.app.extension;

import reactor.core.publisher.Mono;
import run.halo.app.extension.index.IndexedQueryEngine;

/**
 * Reactive extension client for source reads, administration and writes.
 *
 * <p>Read operations belong to {@link ReactiveExtensionReadClient}. This interface adds source mutation and lifecycle
 * operations. Public display consumers should use {@link ReactivePresentationExtensionClient}.
 *
 * @author johnniang
 */
public interface ReactiveExtensionClient extends ReactiveExtensionReadClient {
    @Deprecated(forRemoval = true, since = "2.23.0")
    Mono<JsonExtension> getJsonExtension(GroupVersionKind gvk, String name);

    /**
     * Creates an Extension.
     *
     * @param extension is fresh Extension to be created. Please make sure the Extension name does not exist.
     * @param <E> is Extension type.
     */
    <E extends Extension> Mono<E> create(E extension);

    /**
     * Updates an Extension.
     *
     * @param extension is an Extension to be updated. Please make sure the resource version is latest.
     * @param <E> is Extension type.
     */
    <E extends Extension> Mono<E> update(E extension);

    /**
     * Deletes an Extension.
     *
     * @param extension is an Extension to be deleted. Please make sure the resource version is latest.
     * @param <E> is Extension type.
     */
    <E extends Extension> Mono<E> delete(E extension);

    @Deprecated(forRemoval = true, since = "2.22.0")
    IndexedQueryEngine indexedQueryEngine();

    void watch(Watcher watcher);
}
