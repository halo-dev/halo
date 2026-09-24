package run.halo.app.security.sudo;

import org.springframework.web.server.ServerWebInputException;

/**
 * Generic verification failure for sudo confirmation. Outcomes are not distinguished.
 *
 * @author johnniang
 * @since 2.27.0
 */
public class SudoVerificationFailedException extends ServerWebInputException {

    public SudoVerificationFailedException() {
        super("Verification failed", null, null, "problemDetail.sudo.verificationFailed", null);
    }
}
