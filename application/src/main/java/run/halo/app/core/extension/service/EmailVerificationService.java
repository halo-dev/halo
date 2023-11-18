package run.halo.app.core.extension.service;

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
     * @param username username to obtain email
     */
    Mono<Void> sendVerificationCode(String username);

    /**
     * Verify email by given username and code.
     *
     * @param username username to obtain email
     * @param code code to verify
     * @throws EmailVerificationFailed if send failed
     */
    Mono<Void> verify(String username, String code);
}
