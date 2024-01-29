package run.halo.app.extension.index.query;

import java.util.NavigableSet;

public class IsNotNull extends SimpleQuery {

    protected IsNotNull(String fieldName) {
        super(fieldName, null);
    }

    @Override
    public NavigableSet<String> matches(QueryIndexView indexView) {
        return indexView.getAllIdsForField(fieldName);
    }
}
