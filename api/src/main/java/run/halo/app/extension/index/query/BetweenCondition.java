package run.halo.app.extension.index.query;

record BetweenCondition(String indexName, Object fromKey, boolean fromInclusive, Object toKey, boolean toInclusive)
        implements IndexCondition {

    @Override
    public Condition not() {
        return new NotBetweenCondition(indexName, fromKey, !fromInclusive, toKey, !toInclusive);
    }

    @Override
    public String toString() {
        return indexName + " BETWEEN "
                + (fromInclusive ? "[" : "(")
                + fromKey
                + ", "
                + toKey
                + (toInclusive ? "]" : ")");
    }
}
