package run.halo.app.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;
import org.springframework.web.ErrorResponse;

/**
 * <p>Exception to be thrown when an async request times out.</p>
 * By default the exception will be handled as a {@link HttpStatus#REQUEST_TIMEOUT} error.
 *
 * @author guqing
 * @since 2.0.0
 */
public class AsyncRequestTimeoutException extends RuntimeException implements ErrorResponse {
    public AsyncRequestTimeoutException() {
        super();
    }

    public AsyncRequestTimeoutException(String message) {
        super(message);
    }

    public AsyncRequestTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsyncRequestTimeoutException(Throwable cause) {
        super(cause);
    }

    protected AsyncRequestTimeoutException(String message, Throwable cause,
        boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    @NonNull
    public HttpStatusCode getStatusCode() {
        return HttpStatus.REQUEST_TIMEOUT;
    }

    @Override
    @NonNull
    public ProblemDetail getBody() {
        return ProblemDetail.forStatusAndDetail(getStatusCode(), getMessage());
    }
}
