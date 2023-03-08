package run.halo.app.security.authentication.login;

/**
 * InvalidEncryptedMessageException indicates the encrypted message is invalid.
 *
 * @author johnniang
 */
public class InvalidEncryptedMessageException extends RuntimeException {

    public InvalidEncryptedMessageException(String message) {
        super(message);
    }

    public InvalidEncryptedMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
