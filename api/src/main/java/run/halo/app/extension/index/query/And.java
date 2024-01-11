package run.halo.app.extension.index.query;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.NavigableSet;

public class And extends LogicalQuery {

    /**
     * Creates a new And query with the given child queries.
     *
     * @param childQueries The child queries
     */
    public And(Collection<Query> childQueries) {
        super(childQueries);
        if (this.size < 2) {
            throw new IllegalStateException(
                "An 'And' query cannot have fewer than 2 child queries, " + childQueries.size()
                    + " were supplied");
        }
    }

    @Override
    public NavigableSet<String> matches(QueryIndexView indexView) {
        NavigableSet<String> resultSet = null;
        for (Query query : childQueries) {
            NavigableSet<String> currentResult = query.matches(indexView);
            if (resultSet == null) {
                resultSet = Sets.newTreeSet(currentResult);
            } else {
                resultSet.retainAll(currentResult);
            }
        }
        return resultSet == null ? Sets.newTreeSet() : resultSet;
    }
}
