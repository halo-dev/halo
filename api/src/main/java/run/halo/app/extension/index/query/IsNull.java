package run.halo.app.extension.index.query;

import java.util.NavigableSet;

public class IsNull extends SimpleQuery {

    protected IsNull(String fieldName) {
        super(fieldName, null);
    }

    @Override
    public NavigableSet<String> matches(QueryIndexView indexView) {
        indexView.acquireReadLock();
        try {
            var allIds = indexView.getAllIds();
            var idsForNonNullValue = indexView.getIdsForField(fieldName);
            allIds.removeAll(idsForNonNullValue);
            return allIds;
        } finally {
            indexView.releaseReadLock();
        }
    }

    @Override
    public String toString() {
        return fieldName + " IS NULL";
    }
}
