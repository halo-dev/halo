package run.halo.app.extension.index.query;

record StringContainsCondition(String indexName, String keyword) implements IndexCondition {

    @Override
    public Condition not() {
        return new StringNotContainsCondition(indexName, keyword);
    }

    @Override
    public String toString() {
        return indexName + " CONTAINS " + keyword;
    }
}
