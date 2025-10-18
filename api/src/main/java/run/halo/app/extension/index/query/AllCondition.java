package run.halo.app.extension.index.query;

record AllCondition(String indexName) implements Condition {

    @Override
    public Condition not() {
        return new NoneCondition(indexName);
    }

}
