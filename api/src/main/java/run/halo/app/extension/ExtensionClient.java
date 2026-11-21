package run.halo.app.extension;

import run.halo.app.extension.index.IndexedQueryEngine;

/**
 * Blocking extension client for non-reactive environments.
 *
 * <p>Read operations belong to {@link ExtensionReadClient}. This interface adds source mutation and lifecycle
 * operations. Reactive code should use {@link ReactiveExtensionClient} instead.
 *
 * @author johnniang
 */
public interface ExtensionClient extends ExtensionReadClient {

    /**
     * Creates an Extension.
     *
     * @param extension is fresh Extension to be created. Please make sure the Extension name does not exist.
     * @param <E> is Extension type.
     */
    <E extends Extension> void create(E extension);

    /**
     * Updates an Extension.
     *
     * @param extension is an Extension to be updated. Please make sure the resource version is latest.
     * @param <E> is Extension type.
     */
    <E extends Extension> void update(E extension);

    /**
     * Deletes an Extension.
     *
     * @param extension is an Extension to be deleted. Please make sure the resource version is latest.
     * @param <E> is Extension type.
     */
    <E extends Extension> void delete(E extension);

    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "2.22.0")
    IndexedQueryEngine indexedQueryEngine();

    void watch(Watcher watcher);
}
