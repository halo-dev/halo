package run.halo.app.extension.index.query;

record LabelEqualsCondition(String labelKey, String labelValue) implements LabelCondition {

    @Override
    public Condition not() {
        return new LabelNotEqualsCondition(labelKey, labelValue);
    }
}
