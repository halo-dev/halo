package run.halo.app.extension.index.query;

import java.util.NavigableSet;
import org.springframework.util.Assert;

public class EqualQuery extends SimpleQuery {

    public EqualQuery(String fieldName, String value) {
        super(fieldName, value);
    }

    public EqualQuery(String fieldName, String value, boolean isFieldRef) {
        super(fieldName, value, isFieldRef);
        Assert.notNull(value, "Value must not be null, use IsNull or IsNotNull instead");
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
