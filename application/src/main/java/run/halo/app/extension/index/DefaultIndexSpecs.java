package run.halo.app.extension.index;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang3.StringUtils;

/**
 * Default implementation of {@link IndexSpecs}.
 *
 * @author guqing
 * @since 2.12.0
 */
public class DefaultIndexSpecs implements IndexSpecs {
    private final ConcurrentMap<String, IndexSpec> indexSpecs;

    public DefaultIndexSpecs() {
        this.indexSpecs = new ConcurrentHashMap<>();
    }

    @Override
    public void add(IndexSpec indexSpec) {
        checkIndexSpec(indexSpec);
        var indexName = indexSpec.getName();
        var existingSpec = indexSpecs.putIfAbsent(indexName, indexSpec);
        if (existingSpec != null) {
            throw new IllegalArgumentException(
                "IndexSpec with name " + indexName + " already exists");
        }
    }

    @Override
    public List<IndexSpec> getIndexSpecs() {
        return List.copyOf(this.indexSpecs.values());
    }

    @Override
    public IndexSpec getIndexSpec(String indexName) {
        return this.indexSpecs.get(indexName);
    }

    @Override
    public boolean contains(String indexName) {
        return this.indexSpecs.containsKey(indexName);
    }

    @Override
    public void remove(String name) {
        this.indexSpecs.remove(name);
    }

    private void checkIndexSpec(IndexSpec indexSpec) {
        var order = indexSpec.getOrder();
        if (order == null) {
            indexSpec.setOrder(IndexSpec.OrderType.ASC);
        }
        if (StringUtils.isBlank(indexSpec.getName())) {
            throw new IllegalArgumentException("IndexSpec name must not be blank");
        }
        if (indexSpec.getIndexFunc() == null) {
            throw new IllegalArgumentException("IndexSpec indexFunc must not be null");
        }
    }
}
