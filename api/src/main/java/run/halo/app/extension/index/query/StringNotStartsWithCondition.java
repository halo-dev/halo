package run.halo.app.extension.index.query;

record StringNotStartsWithCondition(String indexName, String prefix) implements IndexCondition {

    @Override
    public Condition not() {
        return new StringStartsWithCondition(indexName, prefix);
    }

}
