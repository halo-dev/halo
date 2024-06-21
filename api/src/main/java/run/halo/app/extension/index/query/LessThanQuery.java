package run.halo.app.extension.index.query;

import java.util.NavigableSet;

public class LessThanQuery extends SimpleQuery {
    private final boolean orEqual;

    public LessThanQuery(String fieldName, String value, boolean orEqual) {
        this(fieldName, value, orEqual, false);
    }

    public LessThanQuery(String fieldName, String value, boolean orEqual, boolean isFieldRef) {
        super(fieldName, value, isFieldRef);
        this.orEqual = orEqual;
    }

    @Override
    public NavigableSet<String> matches(QueryIndexView indexView) {
        if (isFieldRef) {
            return indexView.findMatchingIdsWithSmallerValues(fieldName, value, orEqual);
        }
        return indexView.findIdsLessThan(fieldName, value, orEqual);
    }

    @Override
    public String toString() {
        return fieldName
            + (orEqual ? " <= " : " < ")
            + (isFieldRef ? value : "'" + value + "'");
    }
}
