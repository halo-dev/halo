package run.halo.app.extension.index.query;

record OrCondition(Condition left, Condition right) implements Condition {

    @Override
    public Condition not() {
        return left.not().and(right.not());
    }

}
