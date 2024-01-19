package run.halo.app.extension.index.query;

import com.google.common.collect.Sets;
import java.util.NavigableSet;

public class NotEqual extends SimpleQuery {
    private final EqualQuery equalQuery;

    public NotEqual(String fieldName, String value) {
        this(fieldName, value, false);
    }

    public NotEqual(String fieldName, String value, boolean isFieldRef) {
        super(fieldName, value, isFieldRef);
        this.equalQuery = new EqualQuery(fieldName, value, isFieldRef);
    }

    @Override
    public NavigableSet<String> matches(QueryIndexView indexView) {
        var names = equalQuery.matches(indexView);
        var allNames = indexView.getAllIdsForField(fieldName);

        var resultSet = Sets.<String>newTreeSet();
        for (String name : allNames) {
            if (!names.contains(name)) {
                resultSet.add(name);
            }
        }
        return resultSet;
    }
}
