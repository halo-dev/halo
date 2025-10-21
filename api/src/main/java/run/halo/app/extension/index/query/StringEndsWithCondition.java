package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;

record StringEndsWithCondition(String indexName, String suffix) implements IndexCondition {

    @Override
    public Condition not() {
        return new StringNotEndsWithCondition(indexName, suffix);
    }

    @NotNull
    @Override
    public String toString() {
        return indexName + " ENDS WITH " + suffix;
    }
}
