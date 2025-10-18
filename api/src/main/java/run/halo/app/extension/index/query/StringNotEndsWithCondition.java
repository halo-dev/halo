package run.halo.app.extension.index.query;

record StringNotEndsWithCondition(String indexName, String suffix) implements IndexCondition {

    @Override
    public Condition not() {
        return new StringEndsWithCondition(indexName, suffix);
    }

}
