package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;

record NoneCondition(String indexName) implements IndexCondition {

    @Override
    public Condition not() {
        return new AllCondition(indexName);
    }

    @NotNull
    @Override
    public String toString() {
        return "NONE " + indexName;
    }
}
