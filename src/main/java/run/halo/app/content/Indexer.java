package run.halo.app.content;

import java.util.Set;
import run.halo.app.extension.Extension;

/**
 * <p>Indexer is used to index objects by index name and index key.</p>
 * <p>For example, if you want to index posts by category, you can use the following code:</p>
 * <pre>
 *     Indexer&lt;Post&gt; indexer = new Indexer&lt;&gt;();
 *     indexer.addIndexFunc("category", post -&gt; {
 *       List&lt;String&gt; tags = post.getSpec().getTags();
 *       return tags == null ? Set.of() : Set.copyOf(tags);
 *     });
 *     indexer.add("category", post);
 *     indexer.getByIndex("category", "category-slug");
 *     indexer.update("category", post);
 *     indexer.delete("category", post);
 * </pre>
 *
 * @param <T> the type of object to be indexed
 * @author guqing
 * @since 2.0.0
 */
public interface Indexer<T extends Extension> {

    /**
     * Adds an index function for a given index name.
     *
     * @param indexName The name of the index.
     * @param indexFunc The function to use for indexing.
     */
    void addIndexFunc(String indexName, DefaultIndexer.IndexFunc<T> indexFunc);

    Set<String> indexNames();

    /**
     * The {@code add} method adds an object of type T to the index
     * with the given name. It does this by first getting the index function for the given index
     * name and applying it to the object to get a set of index keys. For each index key, it adds
     * the object key to the index and the index key to the object's index values.
     *
     * <p>For example, if you want to index Person objects by name and age, you can use the
     * following:</p>
     * <pre>
     * // Create an Indexer that indexes Person objects by name and age
     * Indexer&lt;Person&gt; indexer = new Indexer<>();
     * indexer.addIndexFunc("name", person -> Collections.singleton(person.getName()));
     * indexer.addIndexFunc("age", person -> Collections.singleton(String.valueOf(person
     *  .getAge())));
     *
     * // Create some Person objects
     * Person alice = new Person("Alice", 25);
     * Person bob = new Person("Bob", 30);
     *
     * // Add the Person objects to the index
     * indexer.add("name", alice);
     * indexer.add("name", bob);
     * indexer.add("age", alice);
     * indexer.add("age", bob);
     *  </pre>
     *
     * @param indexName The name of the index.
     * @param obj The function to use for indexing.
     * @throws IllegalArgumentException if the index name is not found.
     */
    void add(String indexName, T obj);

    void update(String indexName, T obj);

    Set<String> getByIndex(String indexName, String indexKey);

    void delete(String indexName, T obj);

    @FunctionalInterface
    interface IndexFunc<T> {
        Set<String> apply(T obj);
    }
}
