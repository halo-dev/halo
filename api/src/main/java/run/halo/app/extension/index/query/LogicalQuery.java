package run.halo.app.extension.index.query;

import java.util.Collection;
import java.util.Objects;
import lombok.Getter;

@Getter
public abstract class LogicalQuery implements Query {
    protected final Collection<Query> childQueries;
    protected final int size;

    /**
     * Creates a new logical query with the given child queries.
     *
     * @param childQueries with the given child queries.
     */
    public LogicalQuery(Collection<Query> childQueries) {
        Objects.requireNonNull(childQueries,
            "The child queries supplied to a logical query cannot be null");
        for (Query query : childQueries) {
            if (!isValid(query)) {
                throw new IllegalStateException("Unexpected type of query: " + (query == null ? null
                    : query + ", " + query.getClass()));
            }
        }
        this.size = childQueries.size();
        this.childQueries = childQueries;
    }

    boolean isValid(Query query) {
        return query instanceof LogicalQuery || query instanceof SimpleQuery;
    }
}
