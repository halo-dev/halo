package run.halo.app.extension.router.selector;

import java.util.Objects;
import run.halo.app.extension.index.query.Query;
import run.halo.app.extension.index.query.QueryFactory;

public record FieldSelector(Query query) {
    public FieldSelector(Query query) {
        this.query = Objects.requireNonNullElseGet(query, QueryFactory::all);
    }

    public static FieldSelector of(Query query) {
        return new FieldSelector(query);
    }
}
