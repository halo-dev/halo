package run.halo.app.extension.index.query;

record NotCondition(Condition condition) implements Condition {

    @Override
    public Condition not() {
        return condition;
    }

}
