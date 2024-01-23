package run.halo.app.extension.index.query;

import java.util.NavigableSet;

public class EqualQuery extends SimpleQuery {

    public EqualQuery(String fieldName, String value) {
        super(fieldName, value);
    }

    public EqualQuery(String fieldName, String value, boolean isFieldRef) {
        super(fieldName, value, isFieldRef);
    }

    @Override
    public NavigableSet<String> matches(QueryIndexView indexView) {
        if (isFieldRef) {
            return resultSetForRefValue(indexView);
        }
        return resultSetForExactValue(indexView);
    }

    private NavigableSet<String> resultSetForRefValue(QueryIndexView indexView) {
        return indexView.findIdsForFieldValueEqual(fieldName, value);
    }

    private NavigableSet<String> resultSetForExactValue(QueryIndexView indexView) {
        return indexView.getIdsForFieldValue(fieldName, value);
    }
}
