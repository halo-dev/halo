package run.halo.app.extension.index.query;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.NavigableSet;
import lombok.Getter;

@Getter
public class Not extends LogicalQuery {

    private final Query negatedQuery;

    public Not(Query negatedQuery) {
        super(Collections.singleton(
            requireNonNull(negatedQuery, "The negated query must not be null.")));
        this.negatedQuery = negatedQuery;
    }

    @Override
    public NavigableSet<String> matches(QueryIndexView indexView) {
        var negatedResult = negatedQuery.matches(indexView);
        var allIds = indexView.getAllIds();
        allIds.removeAll(negatedResult);
        return allIds;
    }

    @Override
    public String toString() {
        return "NOT (" + negatedQuery + ")";
    }
}
