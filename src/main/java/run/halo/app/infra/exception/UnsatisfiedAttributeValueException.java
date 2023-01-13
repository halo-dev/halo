package run.halo.app.infra.exception;

import jakarta.validation.constraints.Null;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ServerWebInputException;

/**
 * {@link ServerWebInputException} subclass that indicates an unsatisfied
 * attribute value in request parameters.
 *
 * @author guqing
 * @since 2.2.0
 */
public class UnsatisfiedAttributeValueException extends ServerWebInputException {

    public UnsatisfiedAttributeValueException(String reason, @Nullable String messageDetailCode,
        @Null Object[] messageDetailArguments) {
        super(reason, null, null, messageDetailCode, messageDetailArguments);
    }
}
