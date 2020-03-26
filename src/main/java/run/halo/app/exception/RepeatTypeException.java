package run.halo.app.exception;

/**
 * repeat type exception
 *
 * @author bestsort
 * @date 3/13/20 5:03 PM
 */
public class RepeatTypeException extends ServiceException {
    public RepeatTypeException(String message) {
        super(message);
    }

    public RepeatTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
