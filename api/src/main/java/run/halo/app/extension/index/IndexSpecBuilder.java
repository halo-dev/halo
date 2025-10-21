package run.halo.app.extension.index;

import run.halo.app.extension.Extension;

/**
 * Index specification builder.
 *
 * @param <E> the type of extension
 * @param <K> the type of index key
 * @author johnniang
 * @since 2.22.0
 */
public interface IndexSpecBuilder<
    E extends Extension,
    K extends Comparable<K>,
    B extends IndexSpecBuilder<E, K, B>
    > {

    /**
     * Sets whether the index is unique.
     *
     * @param unique whether the index is unique, default is false
     * @return the updated IndexSpecBuilder
     */
    B unique(boolean unique);

    /**
     * Sets whether the index allows null values.
     *
     * @param nullable whether the index allows null values, default is true
     * @return the updated IndexSpecBuilder
     */
    B nullable(boolean nullable);

    /**
     * Builds the value index specification.
     *
     * @return the value index specification
     */
    ValueIndexSpec<E, K> build();

}
