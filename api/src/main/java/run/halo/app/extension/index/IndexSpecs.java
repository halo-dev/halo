package run.halo.app.extension.index;

import java.util.List;
import org.springframework.lang.Nullable;

/**
 * An interface that defines a collection of {@link IndexSpec}, and provides methods to add,
 * remove, and get {@link IndexSpec}.
 *
 * @author guqing
 * @since 2.12.0
 */
public interface IndexSpecs {

    /**
     * Add a new {@link IndexSpec} to the collection.
     *
     * @param indexSpec the index spec to add.
     * @throws IllegalArgumentException if the index spec with the same name already exists or
     *                                  the index spec is invalid
     */
    void add(IndexSpec indexSpec);

    /**
     * Get all {@link IndexSpec} in the collection.
     *
     * @return all index specs
     */
    List<IndexSpec> getIndexSpecs();

    /**
     * Get the {@link IndexSpec} with the given name.
     *
     * @param indexName the name of the index spec to get.
     * @return the index spec with the given name, or {@code null} if not found.
     */
    @Nullable
    IndexSpec getIndexSpec(String indexName);

    /**
     * Check if the collection contains the {@link IndexSpec} with the given name.
     *
     * @param indexName the name of the index spec to check.
     * @return {@code true} if the collection contains the index spec with the given name,
     */
    boolean contains(String indexName);

    /**
     * Remove the {@link IndexSpec} with the given name.
     *
     * @param name the name of the index spec to remove.
     */
    void remove(String name);
}
