package run.halo.app.extension.index.query;

record GreaterThanCondition(String indexName, Object lowerBound, boolean inclusive)
    implements IndexCondition {

    @Override
    public Condition not() {
        return new LessThanCondition(indexName, lowerBound, !inclusive);
    }
}
