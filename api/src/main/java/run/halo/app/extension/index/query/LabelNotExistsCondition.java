package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;

record LabelNotExistsCondition(String labelKey) implements LabelCondition {

    @Override
    public LabelCondition not() {
        return new LabelExistsCondition(labelKey);
    }

    @NotNull
    @Override
    public String toString() {
        return "NOT EXISTS " + INDEX_NAME + "['" + labelKey + "']";
    }

}
