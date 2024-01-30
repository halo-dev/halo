package run.halo.app.extension.index.query;

import com.google.common.collect.Sets;
import java.util.NavigableSet;
import org.apache.commons.lang3.StringUtils;

public class StringContains extends SimpleQuery {
    public StringContains(String fieldName, String value) {
        super(fieldName, value);
    }

    @Override
    public NavigableSet<String> matches(QueryIndexView indexView) {
        var resultSet = Sets.<String>newTreeSet();
        var fieldValues = indexView.getAllValuesForField(fieldName);
        for (String val : fieldValues) {
            if (StringUtils.containsIgnoreCase(val, value)) {
                resultSet.addAll(indexView.getIdsForFieldValue(fieldName, val));
            }
        }
        return resultSet;
    }
}
