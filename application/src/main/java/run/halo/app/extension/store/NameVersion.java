package run.halo.app.extension.store;

/**
 * Narrow projection of {@link ExtensionStore} containing only name and version, used by index snapshot delta recovery
 * to avoid loading the BLOB data column.
 *
 * @since 2.26.0
 */
public record NameVersion(String name, Long version) {}
