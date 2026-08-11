package run.halo.app.core.user.service;

import reactor.core.publisher.Mono;
import run.halo.app.infra.exception.EmailVerificationFailed;

/**
 * Email verification service to handle email verification.
 *
 * @author guqing
 * @since 2.11.0
 */
public interface EmailVerificationService {

    /**
     * Send verification code by given username.
     *
     * @param username username to verify email must not be blank
     * @param email email to send must not be blank
     */
    Mono<Void> sendVerificationCode(String username, String email);

    /**
     * Verify email by given username and code.
     *
     * @param username username to verify email must not be blank
     * @param code code to verify email must not be blank
     * @throws EmailVerificationFailed if send failed
     */
    Mono<Void> verify(String username, String code);

    /**
     * Send a security verification code to the verified email of the given user.
     *
     * @param username username of the user must not be blank
     */
    Mono<Void> sendSecurityVerificationCode(String username);

    /**
     * Verify the security verification code of the given user. The code is removed after verification and the email
     * binding is never changed.
     *
     * @param username username of the user must not be blank
     * @param code code to verify must not be blank
     * @throws run.halo.app.infra.exception.EmailVerificationFailed if the code is invalid or too many attempts
     */
    Mono<Void> verifySecurityVerificationCode(String username, String code);

    /**
     * Send verification code. The only difference is use email as username.
     *
     * @param email email to send must not be blank
     */
    Mono<Void> sendRegisterVerificationCode(String email);

    /**
     * Verify email by given code.
     *
     * @param email email as username to verify email must not be blank
     * @param code code to verify email must not be blank
     * @throws EmailVerificationFailed if send failed
     */
    Mono<Boolean> verifyRegisterVerificationCode(String email, String code);
}
