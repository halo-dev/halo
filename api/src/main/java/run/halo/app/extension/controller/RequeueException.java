package run.halo.app.extension.controller;

import run.halo.app.extension.controller.Reconciler.Result;


/**
 * Requeue with result data after throwing this exception.
 *
 * @author johnniang
 */
public class RequeueException extends RuntimeException {

    private final Result result;

    public RequeueException(Result result) {
        this(result, null);
    }

    public RequeueException(Result result, String reason) {
        this(result, reason, null);
    }

    public RequeueException(Result result, String reason, Throwable t) {
        super(reason, t);
        this.result = result;
    }

    public Result getResult() {
        return result;
    }
}
