package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;

/**
 * This condition is only for backward compatibility.
 *
 * @param left left condition
 * @param right right condition
 * @deprecated Use {@link Condition#and(Condition)} instead.
 */
@Deprecated(forRemoval = true, since = "2.22.0")
public record And(Condition left, Condition right) implements Condition {

    @Override
    public Condition not() {
        return left.not().or(right.not());
    }

    @NotNull
    @Override
    public String toString() {
        return "(" + left + " AND " + right + ")";
    }
}
