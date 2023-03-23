package run.halo.app.infra.exception;

import jakarta.validation.constraints.Null;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ServerWebInputException;

/**
 * {@link ServerWebInputException} subclass that indicates plugin installation failure.
 *
 * @author guqing
 * @since 2.0.0
 */
public class PluginInstallationException extends ServerWebInputException {

    public PluginInstallationException(String reason, @Nullable String messageDetailCode,
        @Null Object[] messageDetailArguments) {
        super(reason, null, null, messageDetailCode, messageDetailArguments);
    }
}
