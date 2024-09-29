package run.halo.app.core.user.service;

import reactor.core.publisher.Mono;

/**
 * Reset token repository.
 *
 * @author johnniang
 * @since 2.20.0
 */
public interface ResetTokenRepository {

    /**
     * Save reset token.
     *
     * @param resetToken reset token
     * @return empty mono if saved successfully.
     * @throws org.springframework.dao.DuplicateKeyException if token already exists.
     */
    Mono<Void> save(ResetToken resetToken);

    /**
     * Find reset token by token hash.
     *
     * @param tokenHash token hash
     * @return reset token if found, or empty mono.
     */
    Mono<ResetToken> findByTokenHash(String tokenHash);

    /**
     * Remove reset token by token hash.
     *
     * @param tokenHash token hash
     * @return empty mono if removed successfully.
     */
    Mono<Void> removeByTokenHash(String tokenHash);

}
