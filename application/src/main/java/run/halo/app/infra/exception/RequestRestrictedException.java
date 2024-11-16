package run.halo.app.infra.exception;

/**
 * <p>{@link RequestRestrictedException} indicates that the client's request was denied because
 * it did not meet certain required conditions.</p>
 * <p>Typically, this exception is thrown when a user attempts to perform an action that
 * requires prior approval or validation, such as replying to a comment that has not yet been
 * approved.</p>
 * <p>The server understands the request but refuses to process it due to the lack of
 * necessary approval.</p>
 *
 * @author guqing
 * @since 2.20.0
 */
public class RequestRestrictedException extends AccessDeniedException {

    public RequestRestrictedException(String reason) {
        super(reason);
    }

    public RequestRestrictedException(String reason, String detailCode, Object[] detailArgs) {
        super(reason, detailCode, detailArgs);
    }
}
