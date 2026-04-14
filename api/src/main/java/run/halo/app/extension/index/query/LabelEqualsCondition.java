package run.halo.app.extension.index.query;

record LabelEqualsCondition(String labelKey, String labelValue) implements LabelCondition {

    @Override
    public LabelCondition not() {
        return new LabelNotEqualsCondition(labelKey, labelValue);
    }

    @Override
    public String toString() {
        return INDEX_NAME + "['" + labelKey + "'] = '" + labelValue + "'";
    }
}
