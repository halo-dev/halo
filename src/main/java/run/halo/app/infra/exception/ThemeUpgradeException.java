package run.halo.app.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * ThemeUpgradeException will response bad request status if failed to upgrade theme.
 *
 * @author johnniang
 */
public class ThemeUpgradeException extends ResponseStatusException {

    public ThemeUpgradeException(String reason, String detailCode, Object[] detailArgs) {
        super(HttpStatus.BAD_REQUEST, reason, null, detailCode, detailArgs);
    }

}
