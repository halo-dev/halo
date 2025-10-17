package run.halo.app.extension.index.query;

record IsNullCondition(String indexName) implements IndexCondition {

    @Override
    public Condition not() {
        return new IsNotNullCondition(indexName);
    }

}
