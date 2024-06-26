package run.halo.app.security.authentication.rememberme;

import java.time.Instant;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import reactor.core.publisher.Mono;

public interface PersistentRememberMeTokenRepository {
    Mono<Void> createNewToken(PersistentRememberMeToken token);

    Mono<Void> updateToken(String series, String tokenValue, Instant lastUsed);

    Mono<PersistentRememberMeToken> getTokenForSeries(String seriesId);

    Mono<Void> removeUserTokens(String username);
}
