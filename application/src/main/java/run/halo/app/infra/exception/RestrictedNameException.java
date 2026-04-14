package run.halo.app.infra.exception;

import jakarta.validation.constraints.Null;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ServerWebInputException;

/**
 * Restricted name exception.
 *
 * @author lywq
 * @since 2025/10/30 11:47
 **/
public class RestrictedNameException extends ServerWebInputException {

    public RestrictedNameException() {
        super("The name is restricted");
    }

    public RestrictedNameException(String reason, @Nullable String messageDetailCode,
        @Null Object[] messageDetailArguments) {
        super(reason, null, null, messageDetailCode, messageDetailArguments);
    }
}
