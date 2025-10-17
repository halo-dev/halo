package run.halo.app.extension.index.query;

import java.util.Set;
import org.springframework.util.Assert;

record NotInCondition(String indexName, Set<Object> keys) implements IndexCondition {

    public NotInCondition {
        Assert.notNull(keys, "Keys of " + indexName + " must not be empty");
    }

    @Override
    public Condition not() {
        return new InCondition(indexName, keys);
    }
}
