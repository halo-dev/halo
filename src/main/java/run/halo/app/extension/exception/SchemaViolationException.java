package run.halo.app.extension.exception;

import com.networknt.schema.ValidationMessage;
import java.util.Set;

/**
 * This exception is thrown when Schema is violation.
 *
 * @author johnniang
 */
public class SchemaViolationException extends ExtensionException {

    /**
     * Validation errors.
     */
    private final Set<ValidationMessage> errors;

    public SchemaViolationException(Set<ValidationMessage> errors) {
        this.errors = errors;
    }

    public SchemaViolationException(String message, Set<ValidationMessage> errors) {
        super(message);
        this.errors = errors;
    }

    public SchemaViolationException(String message, Throwable cause,
        Set<ValidationMessage> errors) {
        super(message, cause);
        this.errors = errors;
    }

    public SchemaViolationException(Throwable cause, Set<ValidationMessage> errors) {
        super(cause);
        this.errors = errors;
    }

    public SchemaViolationException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace, Set<ValidationMessage> errors) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errors = errors;
    }

    public Set<ValidationMessage> getErrors() {
        return errors;
    }
}
