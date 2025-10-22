package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;

record StringNotContainsCondition(String indexName, String keyword) implements IndexCondition {

    @Override
    public Condition not() {
        return new StringContainsCondition(indexName, keyword);
    }

    @NotNull
    @Override
    public String toString() {
        return indexName + " NOT CONTAINS " + keyword;
    }

}
