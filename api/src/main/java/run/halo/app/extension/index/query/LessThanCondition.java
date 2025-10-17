package run.halo.app.extension.index.query;

record LessThanCondition(String indexName, Object upperBound, boolean inclusive)
    implements IndexCondition {

    @Override
    public Condition not() {
        return new GreaterThanCondition(indexName, upperBound, !inclusive);
    }

}
