package run.halo.app.extension.index.query;

record StringNotContainsCondition(String indexName, String keyword) implements IndexCondition {

    @Override
    public Condition not() {
        return new StringContainsCondition(indexName, keyword);
    }
}
