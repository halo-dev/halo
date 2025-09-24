package run.halo.app.extension.exception;

/**
 * Exception thrown to indicate that the requested operation is not implemented.
 *
 * @author johnniang
 */
public class NotImplementedException extends UnsupportedOperationException {

    public NotImplementedException() {
        this("Not implemented");
    }

    public NotImplementedException(String message) {
        super(message);
    }

}
