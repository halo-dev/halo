package run.halo.app.extension.router.selector;

import java.util.Objects;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import run.halo.app.extension.index.query.Condition;
import run.halo.app.extension.index.query.Queries;
import run.halo.app.extension.index.query.Query;

public record FieldSelector(@Nullable Query query) {

    public FieldSelector(Query query) {
        this.query = Objects.requireNonNullElseGet(query, Queries::empty);
    }

    public static FieldSelector of(Query query) {
        return new FieldSelector(query);
    }

    public static FieldSelector all() {
        return new FieldSelector(Queries.empty());
    }

    public FieldSelector andQuery(Query other) {
        Assert.isInstanceOf(Condition.class, other, "Only Condition query is supported");
        Assert.isInstanceOf(Condition.class, query, "Only Condition query is supported");
        return of(((Condition) query).and((Condition) other));
    }
}
