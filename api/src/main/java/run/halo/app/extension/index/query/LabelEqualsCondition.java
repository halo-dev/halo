package run.halo.app.extension.index.query;

import org.jetbrains.annotations.NotNull;

record LabelEqualsCondition(String labelKey, String labelValue) implements LabelCondition {

    @Override
    public LabelCondition not() {
        return new LabelNotEqualsCondition(labelKey, labelValue);
    }

    @NotNull
    @Override
    public String toString() {
        return INDEX_NAME + "['" + labelKey + "'] = '" + labelValue + "'";
    }
}
