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

}
