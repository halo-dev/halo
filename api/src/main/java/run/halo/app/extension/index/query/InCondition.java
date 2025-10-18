package run.halo.app.extension.index.query;

import java.util.Set;
import org.springframework.util.Assert;

record InCondition(String indexName, Set<Object> keys) implements IndexCondition {

    public InCondition {
        Assert.notNull(keys, "Keys of " + indexName + " must not be empty");
    }

    @Override
    public Condition not() {
        return new NotInCondition(indexName, keys);
    }

}
