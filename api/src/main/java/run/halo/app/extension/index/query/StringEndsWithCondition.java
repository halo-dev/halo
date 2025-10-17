package run.halo.app.extension.index.query;

record StringEndsWithCondition(String indexName, String suffix) implements IndexCondition {

    @Override
    public Condition not() {
        return new StringNotEndsWithCondition(indexName, suffix);
    }

}
