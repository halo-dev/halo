package run.halo.app.security.authentication.rememberme;

import org.springframework.dao.OptimisticLockingFailureException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.RememberMeToken;

public interface PersistentRememberMeTokenRepository {

    Mono<RememberMeToken> createNewToken(RememberMeToken token);

    /**
     * Updates the remember-me token.
     *
     * @param token the remember-me token
     * @return updated remember-me token
     * @throws OptimisticLockingFailureException if the token was concurrently updated by another
     *                                           request.
     *
     */
    Mono<RememberMeToken> updateToken(RememberMeToken token);

    Mono<RememberMeToken> getTokenForSeries(String seriesId);

    Mono<Void> removeUserTokens(String username);

    Mono<Void> removeToken(String series);
}
