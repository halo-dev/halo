package run.halo.app.security.verification;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Thrown when a sensitive operation requires an unexpired security verification. */
public class SecurityVerificationRequiredException extends ResponseStatusException {

    private static final URI TYPE = URI.create("https://halo.run/probs/security-verification-required");

    public static final URI REDIRECT_LOCATION = URI.create("/security-verification");

    public SecurityVerificationRequiredException() {
        super(HttpStatus.FORBIDDEN, "Security verification required");
        setType(TYPE);
        getBody().setProperty("redirectURI", REDIRECT_LOCATION);
    }
}
