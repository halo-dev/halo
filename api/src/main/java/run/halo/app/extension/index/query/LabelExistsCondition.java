package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;

record LabelExistsCondition(String labelKey) implements LabelCondition {

    @Override
    public LabelCondition not() {
        return new LabelNotExistsCondition(labelKey);
    }

    @NotNull
    @Override
    public String toString() {
        return "EXISTS " + INDEX_NAME + "['" + labelKey + "']";
    }

}
