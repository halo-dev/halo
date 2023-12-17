package run.halo.app.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author guqing
 * @author johnniang
 * @since 2.0.0
 */
public class ThemeInstallationException extends ResponseStatusException {

    public ThemeInstallationException(String reason, String detailCode, Object[] detailArgs) {
        super(HttpStatus.BAD_REQUEST, reason, null, detailCode, detailArgs);
    }

}
