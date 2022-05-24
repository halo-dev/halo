package run.halo.app.identity.apitoken;

/**
 * Base exception for all personal access token related errors.
 *
 * @author guqing
 * @since 2.0.0
 */
public class PersonalAccessTokenException extends RuntimeException {

    public PersonalAccessTokenException(String message) {
        super(message);
    }

    public PersonalAccessTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
