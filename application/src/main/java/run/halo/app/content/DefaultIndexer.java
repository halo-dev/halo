package run.halo.app.content;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import run.halo.app.extension.Extension;

/**
 * <p>A default implementation of {@link Indexer}.</p>
 * <p>Note that this Indexer is not thread-safe, If multiple threads access this indexer
 * concurrently and one of the threads modifies the indexer, it must be synchronized externally.</p>
 *
 * @param <T> the type of object to be indexed
 * @author guqing
 * @see
 * <a href="https://github.com/kubernetes/kubernetes/blob/master/staging/src/k8s.io/client-go/tools/cache/index.go">kubernetes index</a>
 * @see <a href="https://juejin.cn/post/7132767272841510926">informer机制之cache.indexer机制</a>
 * @since 2.0.0
 */
public class DefaultIndexer<T extends Extension> implements Indexer<T> {
    private final Map<String, SetMultimap<String, String>> indices = new HashMap<>();
    private final Map<String, IndexFunc<T>> indexFuncMap = new HashMap<>();
    private final Map<String, SetMultimap<String, String>> indexValues = new HashMap<>();

    @Override
    public void addIndexFunc(String indexName, IndexFunc<T> indexFunc) {
        indexFuncMap.put(indexName, indexFunc);
        indices.put(indexName, HashMultimap.create());
        indexValues.put(indexName, HashMultimap.create());
    }

    @Override
    public Set<String> indexNames() {
        return Set.copyOf(indexFuncMap.keySet());
    }

    @Override
    public void add(String indexName, T obj) {
        IndexFunc<T> indexFunc = getIndexFunc(indexName);
        Set<String> indexKeys = indexFunc.apply(obj);
        for (String indexKey : indexKeys) {
            SetMultimap<String, String> index = indices.get(indexName);
            index.put(indexKey, getObjectKey(obj));

            SetMultimap<String, String> indexValue = indexValues.get(indexName);
            indexValue.put(getObjectKey(obj), indexKey);
        }
    }

    @NonNull
    private IndexFunc<T> getIndexFunc(String indexName) {
        IndexFunc<T> indexFunc = indexFuncMap.get(indexName);
        if (indexFunc == null) {
            throw new IllegalArgumentException(
                "Index function not found for index name: " + indexName);
        }
        return indexFunc;
    }

    @Override
    public void update(String indexName, T obj) {
        IndexFunc<T> indexFunc = getIndexFunc(indexName);
        Set<String> indexKeys = indexFunc.apply(obj);
        Set<String> oldIndexKeys = new HashSet<>();
        SetMultimap<String, String> indexValue = indexValues.get(indexName);
        if (indexValue.containsKey(getObjectKey(obj))) {
            oldIndexKeys.addAll(indexValue.get(getObjectKey(obj)));
        }
        // delete old index first
        for (String oldIndexKey : oldIndexKeys) {
            SetMultimap<String, String> index = indices.get(indexName);
            index.remove(oldIndexKey, getObjectKey(obj));
            indexValue.remove(getObjectKey(obj), oldIndexKey);
        }
        // add new index
        for (String indexKey : indexKeys) {
            SetMultimap<String, String> index = indices.get(indexName);
            index.put(indexKey, getObjectKey(obj));

            indexValue.put(getObjectKey(obj), indexKey);
        }
    }

    @Override
    public Set<String> getByIndex(String indexName, String indexKey) {
        SetMultimap<String, String> index = indices.get(indexName);
        if (index != null) {
            return Set.copyOf(index.get(indexKey));
        }
        return Set.of();
    }

    @Override
    public void delete(String indexName, T obj) {
        IndexFunc<T> indexFunc = getIndexFunc(indexName);
        SetMultimap<String, String> indexValue = indexValues.get(indexName);
        Set<String> indexKeys = indexFunc.apply(obj);
        for (String indexKey : indexKeys) {
            String objectKey = getObjectKey(obj);
            SetMultimap<String, String> index = indices.get(indexName);
            index.remove(indexKey, objectKey);

            indexValue.remove(indexKey, objectKey);
        }
    }

    /**
     * This method is only used for testing.
     *
     * @param indexName index name
     * @return all indices of the given index name
     */
    public Map<String, Collection<String>> getIndices(String indexName) {
        return indices.get(indexName).asMap();
    }

    private String getObjectKey(T obj) {
        Assert.notNull(obj, "Object must not be null");
        Assert.notNull(obj.getMetadata(), "Object metadata must not be null");
        Assert.notNull(obj.getMetadata().getName(), "Object name must not be null");
        return obj.getMetadata().getName();
    }
}
