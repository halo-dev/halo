package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;

record LabelNotEqualsCondition(String labelKey, String labelValue) implements LabelCondition {

    @Override
    public LabelCondition not() {
        return new LabelEqualsCondition(labelKey, labelValue);
    }

    @NotNull
    @Override
    public String toString() {
        return INDEX_NAME + "['" + labelKey + "'] <> '" + labelValue + "'";
    }

}
