package run.halo.app.security.authentication.twofactor;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class TwoFactorAuthRequiredException extends ResponseStatusException {

    private static final URI type = URI.create("https://halo.run/probs/2fa-required");

    public TwoFactorAuthRequiredException(URI redirectURI) {
        super(HttpStatus.UNAUTHORIZED, "Two-factor authentication required");
        setType(type);
        getBody().setProperty("redirectURI", redirectURI);
    }

}
