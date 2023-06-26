package run.halo.app.infra.exception;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

public class RateLimitExceededException extends ResponseStatusException {

    public RateLimitExceededException(@Nullable Throwable cause) {
        super(HttpStatus.TOO_MANY_REQUESTS, "You have exceeded your quota", cause);
        setType(URI.create(Exceptions.REQUEST_NOT_PERMITTED_TYPE));
    }

}
