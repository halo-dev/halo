package run.halo.app.extension.index.query;

record LabelNotEqualsCondition(String labelKey, String labelValue) implements LabelCondition {

    @Override
    public Condition not() {
        return new LabelEqualsCondition(labelKey, labelValue);
    }
}
