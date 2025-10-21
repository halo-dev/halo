package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;

record IsNullCondition(String indexName) implements IndexCondition {

    @Override
    public Condition not() {
        return new IsNotNullCondition(indexName);
    }

    @NotNull
    @Override
    public String toString() {
        return indexName + " IS NULL";
    }
}
