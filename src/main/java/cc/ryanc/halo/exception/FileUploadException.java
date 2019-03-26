package cc.ryanc.halo.exception;

/**
 * File upload exception.
 *
 * @author johnniang
 * @date 3/27/19
 */
public class FileUploadException extends ServiceException {
    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
