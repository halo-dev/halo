package run.halo.app.extension.index.query;

record EmptyCondition() implements Condition {

    @Override
    public String toString() {
        return "EMPTY";
    }
}
