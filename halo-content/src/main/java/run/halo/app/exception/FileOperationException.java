package run.halo.app.exception;

/**
 * File operation exception.
 *
 * @author johnniang
 * @date 3/27/19
 */
public class FileOperationException extends ServiceException {
    public FileOperationException(String message) {
        super(message);
    }

    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
