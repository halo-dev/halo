package run.halo.app.extension.exception;

import org.openapi4j.core.validation.ValidationResults;

/**
 * This exception is thrown when Schema is violation.
 *
 * @author johnniang
 */
public class SchemaViolationException extends ExtensionException {

    /**
     * Validation errors.
     */
    private final ValidationResults errors;

    public SchemaViolationException(ValidationResults errors) {
        this.errors = errors;
    }

    public SchemaViolationException(String message, ValidationResults errors) {
        super(message);
        this.errors = errors;
    }

    public SchemaViolationException(String message, Throwable cause, ValidationResults errors) {
        super(message, cause);
        this.errors = errors;
    }

    public SchemaViolationException(Throwable cause, ValidationResults errors) {
        super(cause);
        this.errors = errors;
    }

    public SchemaViolationException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace, ValidationResults errors) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errors = errors;
    }

    public ValidationResults getErrors() {
        return errors;
    }
}
