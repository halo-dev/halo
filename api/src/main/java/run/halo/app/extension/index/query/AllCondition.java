package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;

record AllCondition(String indexName) implements Condition {

    @Override
    public Condition not() {
        return new NoneCondition(indexName);
    }

    @NotNull
    @Override
    public String toString() {
        return "ALL " + indexName;
    }

}
