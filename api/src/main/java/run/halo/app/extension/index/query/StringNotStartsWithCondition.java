package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;

record StringNotStartsWithCondition(String indexName, String prefix) implements IndexCondition {

    @Override
    public Condition not() {
        return new StringStartsWithCondition(indexName, prefix);
    }

    @NotNull
    @Override
    public String toString() {
        return indexName + " NOT STARTS WITH " + prefix;
    }
}
