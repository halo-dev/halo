package run.halo.app.extension.index.query;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.NavigableSet;
import java.util.stream.Collectors;

public class Or extends LogicalQuery {

    public Or(Collection<Query> childQueries) {
        super(childQueries);
    }

    @Override
    public NavigableSet<String> matches(QueryIndexView indexView) {
        var resultSet = Sets.<String>newTreeSet();
        for (Query query : childQueries) {
            resultSet.addAll(query.matches(indexView));
        }
        return resultSet;
    }

    @Override
    public String toString() {
        return "(" + childQueries.stream().map(Query::toString)
            .collect(Collectors.joining(" OR ")) + ")";
    }
}
