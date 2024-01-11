package run.halo.app.extension.index.query;

import com.google.common.collect.Sets;
import java.util.NavigableSet;

public class StringEndsWith extends SimpleQuery {
    public StringEndsWith(String fieldName, String value) {
        super(fieldName, value);
    }

    @Override
    public NavigableSet<String> matches(QueryIndexView indexView) {
        var resultSet = Sets.<String>newTreeSet();
        var fieldValues = indexView.getAllValuesForField(fieldName);
        for (String val : fieldValues) {
            if (val.endsWith(value)) {
                resultSet.addAll(indexView.getIdsForFieldValue(fieldName, val));
            }
        }
        return resultSet;
    }
}
