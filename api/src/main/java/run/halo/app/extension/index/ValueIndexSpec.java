package run.halo.app.extension.index;

import run.halo.app.extension.Extension;

/**
 * Specification for a value index on an extension.
 *
 * @param <E> the type of the extension
 * @param <K> the type of the key
 * @author johnniang
 * @since 2.22.0
 */
public interface ValueIndexSpec<E extends Extension, K extends Comparable<K>> {

    /**
     * Gets the name of this index.
     *
     * @return the name of this index
     */
    String getName();

    /**
     * Whether this index is unique.
     *
     * @return true if this index is unique, false otherwise
     */
    boolean isUnique();

    /**
     * Whether this index allows null values.
     *
     * @return true if this index allows null values, false otherwise
     */
    boolean isNullable();

    /**
     * Gets the type of the key.
     *
     * @return the type of the key
     */
    Class<K> getKeyType();

    /**
     * Gets the version of this index spec. Bump the version to trigger a rebuild of the index when the semantics of the
     * index function change without any structural change.
     *
     * @return the version of this index spec, defaults to 1
     * @since 2.26.0
     */
    default int getVersion() {
        return 1;
    }
}
