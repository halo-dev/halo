package run.halo.app.extension.index.query;

record LabelNotExistsCondition(String labelKey) implements LabelCondition {

    @Override
    public LabelCondition not() {
        return new LabelExistsCondition(labelKey);
    }

    @Override
    public String toString() {
        return "NOT EXISTS " + INDEX_NAME + "['" + labelKey + "']";
    }

}
