package run.halo.app.extension.index.query;

record NoneCondition(String indexName) implements IndexCondition {

    @Override
    public Condition not() {
        return new AllCondition(indexName);
    }

}
