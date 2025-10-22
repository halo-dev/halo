package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

record AndCondition(Condition left, Condition right) implements Condition {

    public AndCondition {
        Assert.notNull(left, "Left condition must not be null");
        Assert.notNull(right, "Right condition must not be null");
    }

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
