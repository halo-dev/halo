package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

record EqualCondition(String indexName, Object key) implements IndexCondition {

    public EqualCondition {
        Assert.notNull(key, "Key of " + indexName + " must not be null");
    }

    @Override
    public Condition not() {
        return new NotEqualCondition(indexName, key);
    }

    @NotNull
    @Override
    public String toString() {
        return indexName + " = " + key;
    }
}
