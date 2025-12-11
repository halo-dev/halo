package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;

record StringNotEndsWithCondition(String indexName, String suffix) implements IndexCondition {

    @Override
    public Condition not() {
        return new StringEndsWithCondition(indexName, suffix);
    }

    @NotNull
    @Override
    public String toString() {
        return indexName + " NOT ENDS WITH " + suffix;
    }
}
