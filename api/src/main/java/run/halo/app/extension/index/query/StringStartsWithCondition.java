package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;

record StringStartsWithCondition(String indexName, String prefix) implements IndexCondition {

    @Override
    public Condition not() {
        return new StringNotStartsWithCondition(indexName, prefix);
    }

    @NotNull
    @Override
    public String toString() {
        return indexName + " STARTS WITH " + prefix;
    }
}
