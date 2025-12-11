package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;

record StringContainsCondition(String indexName, String keyword) implements IndexCondition {

    @Override
    public Condition not() {
        return new StringNotContainsCondition(indexName, keyword);
    }

    @NotNull
    @Override
    public String toString() {
        return indexName + " CONTAINS " + keyword;
    }
}
