package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;

record EmptyCondition() implements Condition {

    @NotNull
    @Override
    public String toString() {
        return "EMPTY";
    }

}
