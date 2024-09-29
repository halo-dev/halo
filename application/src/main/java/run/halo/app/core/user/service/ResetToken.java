package run.halo.app.core.user.service;

import java.time.Instant;

/**
 * Reset token data.
 *
 * @param tokenHash The token hash
 * @param username The username
 * @param expiresAt The expires at
 * @author johnniang
 * @since 2.20.0
 */
public record ResetToken(String tokenHash, String username, Instant expiresAt) {
}
