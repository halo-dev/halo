package run.halo.app.extension.index.query;

import com.google.common.collect.Sets;
import java.util.NavigableSet;
import java.util.Set;

public class InQuery extends SimpleQuery {
    private final Set<String> values;

    public InQuery(String columnName, Set<String> values) {
        super(columnName, null);
        this.values = values;
    }

    @Override
    public NavigableSet<String> matches(QueryIndexView indexView) {
        NavigableSet<String> resultSet = Sets.newTreeSet();
        for (String val : values) {
            resultSet.addAll(indexView.getIdsForFieldValue(fieldName, val));
        }
        return resultSet;
    }
}
