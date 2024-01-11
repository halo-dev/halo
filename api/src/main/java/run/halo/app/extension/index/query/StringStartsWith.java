package run.halo.app.extension.index.query;

import com.google.common.collect.Sets;
import java.util.NavigableSet;

public class StringStartsWith extends SimpleQuery {
    public StringStartsWith(String fieldName, String value) {
        super(fieldName, value);
    }

    @Override
    public NavigableSet<String> matches(QueryIndexView indexView) {
        var resultSet = Sets.<String>newTreeSet();
        var allValues = indexView.getAllValuesForField(fieldName);

        for (String val : allValues) {
            if (val.startsWith(value)) {
                resultSet.addAll(indexView.getIdsForFieldValue(fieldName, val));
            }
        }
        return resultSet;
    }
}
