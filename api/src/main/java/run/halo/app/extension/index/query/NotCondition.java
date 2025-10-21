package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

record NotCondition(Condition condition) implements Condition {

    public NotCondition {
        Assert.notNull(condition, "Condition must not be null");
    }

    @Override
    public Condition not() {
        return condition;
    }

    @NotNull
    @Override
    public String toString() {
        return "NOT (" + condition + ")";
    }
}
