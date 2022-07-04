package run.halo.app.extension.controller;

import java.time.Duration;

public interface Reconciler {

    Result reconcile(Request request);

    record Request(String name) {
    }

    record Result(boolean reEnqueue, Duration retryAfter) {

    }
}
