package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;

record LessThanCondition(String indexName, Object upperBound, boolean inclusive)
    implements IndexCondition {

    @Override
    public Condition not() {
        return new GreaterThanCondition(indexName, upperBound, !inclusive);
    }

    @NotNull
    @Override
    public String toString() {
        return indexName + (inclusive ? " <= " : " < ") + upperBound;
    }
}
