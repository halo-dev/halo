package run.halo.app.extension.index.query;

import com.google.common.collect.Sets;
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
            return resultSetForRefValue(indexView);
        }
        return resultSetForExtractValue(indexView);
    }

    private NavigableSet<String> resultSetForRefValue(QueryIndexView indexView) {
        return indexView.findIdsForFieldValueGreaterThan(fieldName, value, orEqual);
    }

    private NavigableSet<String> resultSetForExtractValue(QueryIndexView indexView) {
        var resultSet = Sets.<String>newTreeSet();
        var allValues = indexView.getAllValuesForField(fieldName);
        NavigableSet<String> tailSet =
            orEqual ? allValues.tailSet(value, true) : allValues.tailSet(value, false);

        for (String val : tailSet) {
            resultSet.addAll(indexView.getIdsForFieldValue(fieldName, val));
        }
        return resultSet;
    }
}
