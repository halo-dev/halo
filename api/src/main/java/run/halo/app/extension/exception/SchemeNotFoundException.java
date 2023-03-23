package run.halo.app.extension.exception;

import org.springframework.http.HttpStatus;
import run.halo.app.extension.GroupVersionKind;

/**
 * SchemeNotFoundException is thrown while we try to get a scheme but not found.
 *
 * @author johnniang
 */
public class SchemeNotFoundException extends ExtensionException {

    public SchemeNotFoundException(GroupVersionKind gvk) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Scheme not found for " + gvk, null, null,
            new Object[] {gvk});
    }

}
