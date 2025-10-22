package run.halo.app.extension.index;

import java.util.List;
import run.halo.app.extension.Extension;

/**
 * An interface that defines a collection of {@link IndexSpec}, and provides methods to add,
 * remove, and get {@link IndexSpec}.
 *
 * @author guqing
 * @since 2.12.0
 */
public interface IndexSpecs<E extends Extension> {

    /**
     * Add a new {@link IndexSpec} to the collection.
     *
     * @param indexSpec the index spec to add.
     * @throws IllegalArgumentException if the index spec with the same name already exists or
     *                                  the index spec is invalid
     */
    default <K extends Comparable<K>> void add(IndexSpec<E, K> indexSpec) {
        add((ValueIndexSpec<E, K>) indexSpec);
    }

    <K extends Comparable<K>> void add(ValueIndexSpec<E, K> indexSpec);

    default <K extends Comparable<K>> void add(IndexSpecBuilder<E, K, ?> builder) {
        add(builder.build());
    }

    /**
     * Get all {@link IndexSpec} in the collection.
     *
     * @return all index specs
     */
    List<ValueIndexSpec<E, ?>> getIndexSpecs();

    /***
     * Create a multi-value index spec builder.
     *
     * @param name the name of the index spec
     * @param keyType the type of the keys used in the index spec
     * @param <E> the type of the extension
     * @param <K> the type of the key
     * @return a MultiValueBuilder for the specified index spec
     */
    static <E extends Extension, K extends Comparable<K>> MultiValueIndexSpecBuilder<E, K> multi(
        String name,
        Class<K> keyType
    ) {
        return new MultiValueBuilder<>(name, keyType);
    }

    /**
     * Create a single-value index spec builder.
     *
     * @param name the name of the index spec
     * @param keyType the type of the keys used in the index spec
     * @param <E> the type of the extension
     * @param <K> the type of the key
     * @return a SingleValueBuilder for the specified index spec.
     */
    static <E extends Extension, K extends Comparable<K>> SingleValueIndexSpecBuilder<E, K> single(
        String name,
        Class<K> keyType
    ) {
        return new SingleValueBuilder<>(name, keyType);
    }

}
