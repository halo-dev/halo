package run.halo.app.extension.index.query;

record LabelExistsCondition(String labelKey) implements LabelCondition {

    @Override
    public Condition not() {
        return new LabelNotExistsCondition(labelKey);
    }

}
