package run.halo.app.extension.exception;

import com.networknt.schema.Error;
import java.util.List;
import org.springframework.http.HttpStatus;
import run.halo.app.extension.GroupVersionKind;

/**
 * This exception is thrown when Schema is violation.
 *
 * @author johnniang
 */
public class SchemaViolationException extends ExtensionException {

    /** Validation errors. */
    private final List<Error> errors;

    public SchemaViolationException(GroupVersionKind gvk, List<Error> errors) {
        super(HttpStatus.BAD_REQUEST, "Failed to validate " + gvk, null, null, new Object[] {gvk, errors});
        this.errors = List.copyOf(errors);
    }

    public List<Error> getErrors() {
        return errors;
    }
}
