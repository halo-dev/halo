package run.halo.app.extension.index.query;

import java.util.Collection;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

record NotInCondition(String indexName, Collection<Object> keys) implements IndexCondition {

    public NotInCondition {
        Assert.notNull(keys, "Keys of " + indexName + " must not be empty");
    }

    @Override
    public Condition not() {
        return new InCondition(indexName, keys);
    }

    @NotNull
    @Override
    public String toString() {
        return indexName + " NOT IN ("
            + keys.stream().map(Object::toString).collect(Collectors.joining(", "))
            + ")";
    }
}
