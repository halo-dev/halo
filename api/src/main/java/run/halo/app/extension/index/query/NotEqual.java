package run.halo.app.extension.index.query;

import com.google.common.collect.Sets;
import java.util.NavigableSet;
import org.springframework.util.Assert;

public class NotEqual extends SimpleQuery {
    private final EqualQuery equalQuery;

    public NotEqual(String fieldName, String value) {
        this(fieldName, value, false);
    }

    public NotEqual(String fieldName, String value, boolean isFieldRef) {
        super(fieldName, value, isFieldRef);
        Assert.notNull(value, "Value must not be null, use IsNull or IsNotNull instead");
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
