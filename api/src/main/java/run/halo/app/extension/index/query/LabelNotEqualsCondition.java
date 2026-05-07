package run.halo.app.extension.index.query;

record LabelNotEqualsCondition(String labelKey, String labelValue) implements LabelCondition {

    @Override
    public LabelCondition not() {
        return new LabelEqualsCondition(labelKey, labelValue);
    }

    @Override
    public String toString() {
        return INDEX_NAME + "['" + labelKey + "'] <> '" + labelValue + "'";
    }
}
