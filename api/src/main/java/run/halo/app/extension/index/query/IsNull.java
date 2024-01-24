package run.halo.app.extension.index.query;

import java.util.NavigableSet;

public class IsNull extends SimpleQuery {

    protected IsNull(String fieldName) {
        super(fieldName, null);
    }

    @Override
    public NavigableSet<String> matches(QueryIndexView indexView) {
        var allIds = indexView.getAllIds();
        var idsForField = indexView.getAllIdsForField(fieldName);
        allIds.removeAll(idsForField);
        return allIds;
    }
}
