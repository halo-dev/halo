package run.halo.app.security.authentication.emailcode;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;

/**
 * Service for managing email login verification codes.
 *
 * @author johnniang
 * @since 2.26.0
 */
public interface EmailCodeService {

    /**
     * Generates and sends a login verification code to the given verified email address.
     *
     * @param email the verified email address of an existing user
     * @return a Mono that completes when the code has been sent
     */
    Mono<Void> sendLoginCode(String email);

    /**
     * Verifies the login code for the given email and returns the associated user.
     *
     * @param email the email address
     * @param code the verification code
     * @return the user if the code is valid, otherwise an error
     */
    Mono<User> verifyLoginCode(String email, String code);
}
