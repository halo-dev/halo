package run.halo.app.extension.index.query;

import com.google.common.collect.Sets;
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
            return resultSetForRefValue(indexView);
        }
        return resultSetForExactValue(indexView);
    }

    private NavigableSet<String> resultSetForRefValue(QueryIndexView indexView) {
        return indexView.findIdsForFieldValueLessThan(fieldName, value, orEqual);
    }

    private NavigableSet<String> resultSetForExactValue(QueryIndexView indexView) {
        var resultSet = Sets.<String>newTreeSet();
        var allValues = indexView.getAllValuesForField(fieldName);
        var headSet = orEqual ? allValues.headSet(value, true)
            : allValues.headSet(value, false);

        for (String val : headSet) {
            resultSet.addAll(indexView.getIdsForFieldValue(fieldName, val));
        }
        return resultSet;
    }
}
