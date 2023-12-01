package run.halo.app.core.extension.service;

import reactor.core.publisher.Mono;
import run.halo.app.infra.exception.AccessDeniedException;

/**
 * An interface for email password recovery.
 *
 * @author guqing
 * @since 2.11.0
 */
public interface EmailPasswordRecoveryService {

    /**
     * <p>Send password reset email.</p>
     * if the user does not exist, it will return {@link Mono#empty()}
     * if the user exists, but the email is not the same, it will return {@link Mono#empty()}
     *
     * @param username username to request password reset
     * @param email email to match the user with the username
     * @return {@link Mono#empty()} if the user does not exist, or the email is not the same.
     */
    Mono<Void> sendPasswordResetEmail(String username, String email);

    /**
     * <p>Reset password by token.</p>
     * if the token is invalid, it will return {@link Mono#error(Throwable)}}
     * if the token is valid, but the username is not the same, it will return
     * {@link Mono#error(Throwable)}
     *
     * @param username username to reset password
     * @param newPassword new password
     * @param token token to validate the user
     * @return {@link Mono#empty()} if the token is invalid or the username is not the same.
     * @throws AccessDeniedException if the token is invalid
     */
    Mono<Void> changePassword(String username, String newPassword, String token);
}
