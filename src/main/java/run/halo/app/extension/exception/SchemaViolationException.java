package run.halo.app.extension.exception;

import org.openapi4j.core.validation.ValidationResults;
import org.springframework.http.HttpStatus;
import run.halo.app.extension.GroupVersionKind;

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

    public SchemaViolationException(GroupVersionKind gvk, ValidationResults errors) {
        super(HttpStatus.BAD_REQUEST, "Failed to validate " + gvk, null, null,
            new Object[] {gvk, errors});
        this.errors = errors;
    }

    public ValidationResults getErrors() {
        return errors;
    }
}
