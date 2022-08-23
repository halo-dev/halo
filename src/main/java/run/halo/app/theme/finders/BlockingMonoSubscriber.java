package run.halo.app.theme.finders;

public final class BlockingMonoSubscriber<T> extends BlockingSingleSubscriber<T> {
    @Override
    public void onNext(T t) {
        if (value == null) {
            value = t;
            countDown();
        }
    }

    @Override
    public void onError(Throwable t) {
        if (value == null) {
            error = t;
        }
        countDown();
    }

    @Override
    public String stepName() {
        return "block";
    }
}
