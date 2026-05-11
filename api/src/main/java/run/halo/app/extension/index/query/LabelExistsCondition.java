package run.halo.app.extension.index.query;

record LabelExistsCondition(String labelKey) implements LabelCondition {

    @Override
    public LabelCondition not() {
        return new LabelNotExistsCondition(labelKey);
    }

    @Override
    public String toString() {
        return "EXISTS " + INDEX_NAME + "['" + labelKey + "']";
    }
}
