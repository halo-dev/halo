package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;

record IsNotNullCondition(String indexName) implements IndexCondition {

    @Override
    public Condition not() {
        return new IsNullCondition(indexName);
    }

    @NotNull
    @Override
    public String toString() {
        return indexName + " IS NOT NULL";
    }

}
