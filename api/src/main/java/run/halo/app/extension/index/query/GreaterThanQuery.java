package run.halo.app.extension.index.query;

import java.util.NavigableSet;

public class GreaterThanQuery extends SimpleQuery {
    private final boolean orEqual;

    public GreaterThanQuery(String fieldName, String value, boolean orEqual) {
        this(fieldName, value, orEqual, false);
    }

    public GreaterThanQuery(String fieldName, String value, boolean orEqual, boolean isFieldRef) {
        super(fieldName, value, isFieldRef);
        this.orEqual = orEqual;
    }

    @Override
    public NavigableSet<String> matches(QueryIndexView indexView) {
        if (isFieldRef) {
            return indexView.findMatchingIdsWithGreaterValues(fieldName, value, orEqual);
        }
        return indexView.findIdsGreaterThan(fieldName, value, orEqual);
    }

    @Override
    public String toString() {
        return fieldName
            + (orEqual ? " >= " : " > ")
            + (isFieldRef ? value : "'" + value + "'");
    }
}
