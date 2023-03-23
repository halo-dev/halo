package run.halo.app.infra.utils;

/**
 * {@link JsonParseException} thrown when source JSON is invalid.
 *
 * @author guqing
 * @since 2.0.0
 */
public class JsonParseException extends RuntimeException {
    public JsonParseException() {
        super();
    }

    public JsonParseException(String message) {
        super(message);
    }

    public JsonParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonParseException(Throwable cause) {
        super(cause);
    }

    protected JsonParseException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
