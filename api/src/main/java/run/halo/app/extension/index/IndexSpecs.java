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

    default <K extends Comparable<K>> void add(IndexSpecBuilder<E, K> builder) {
        add(builder.build());
    }

    /**
     * Get all {@link IndexSpec} in the collection.
     *
     * @return all index specs
     */
    List<ValueIndexSpec<E, ?>> getIndexSpecs();

}
