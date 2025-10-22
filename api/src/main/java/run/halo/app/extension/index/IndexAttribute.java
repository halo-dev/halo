package run.halo.app.extension.index;

import java.util.Set;
import run.halo.app.extension.Extension;

/**
 *
 * An attribute used for indexing extensions.
 *
 * @param <E> the type of the extension
 * @param <K> the type of the key
 * @deprecated Use {@link ValueIndexSpec} instead.
 */
@Deprecated(forRemoval = true, since = "2.22.0")
public interface IndexAttribute<E extends Extension, K extends Comparable<K>> {

    /**
     * Specify this class is belonged to which extension.
     *
     * @return the extension class.
     */
    Class<E> getObjectType();

    /**
     * Gets the value type of the attribute.
     *
     * @return the value type of the attribute.
     */
    Class<K> getKeyType();

    /**
     * Gets the values of the attribute from the given extension.
     *
     * @param e the extension
     * @return the values of the attribute
     * @throws IllegalArgumentException if the given extension is not of the expected type
     */
    Set<K> getValues(E e);

}
