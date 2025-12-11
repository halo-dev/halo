package run.halo.app.extension.index.query;

import org.springframework.data.relational.core.sql.Visitable;

/**
 * A condition used in querying index.
 * e.g.: {@code metadata.name = 'halo' AND status.published = true}
 *
 * @author johnniang
 * @since 2.22.0
 */
public interface Condition extends Visitable, Query {

    /**
     * Combine with another condition using AND operator.
     *
     * @param another another condition
     * @return the combined condition
     */
    default Condition and(Condition another) {
        return new AndCondition(this, another);
    }

    /**
     * Combine with another condition using OR operator.
     *
     * @param another another condition
     * @return the combined condition
     */
    default Condition or(Condition another) {
        return new OrCondition(this, another);
    }

    /**
     * Negate this condition.
     *
     * @return the negated condition
     */
    default Condition not() {
        return new NotCondition(this);
    }

    /**
     * Creates an empty condition.
     *
     * @return an empty condition
     */
    static Condition empty() {
        return new EmptyCondition();
    }

}
