package run.halo.app.extension.router.selector;

import java.util.Objects;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import run.halo.app.extension.index.query.Query;
import run.halo.app.extension.index.query.QueryFactory;

public record FieldSelector(@NonNull Query query) {
    public FieldSelector(Query query) {
        this.query = Objects.requireNonNullElseGet(query, QueryFactory::all);
    }

    public static FieldSelector of(Query query) {
        return new FieldSelector(query);
    }

    public static FieldSelector all() {
        return new FieldSelector(QueryFactory.all());
    }

    public FieldSelector andQuery(Query other) {
        Assert.notNull(other, "Query must not be null");
        return of(QueryFactory.and(query(), other));
    }
}
