package run.halo.app.extension.index.query;

import org.springframework.data.relational.core.sql.Visitable;

/**
 * A condition used in querying index.
 * metadata.name = 'halo' AND status.published = true
 *
 * @author johnniang
 * @since 2.22.0
 */
public interface Condition extends Visitable, Query {

    default Condition and(Condition another) {
        return new AndCondition(this, another);
    }

    default Condition or(Condition another) {
        return new OrCondition(this, another);
    }

    default Condition not() {
        return new NotCondition(this);
    }

    static Condition empty() {
        return new EmptyCondition();
    }

}
