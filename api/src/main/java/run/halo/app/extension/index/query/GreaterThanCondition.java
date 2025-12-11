package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;

record GreaterThanCondition(String indexName, Object lowerBound, boolean inclusive)
    implements IndexCondition {

    @Override
    public Condition not() {
        return new LessThanCondition(indexName, lowerBound, !inclusive);
    }

    @NotNull
    @Override
    public String toString() {
        return indexName + (inclusive ? " >= " : " > ") + lowerBound;
    }
}
