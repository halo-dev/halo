package run.halo.app.core.user.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Objects;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * In-memory reset token repository.
 *
 * @author johnniang
 * @since 2.20.0
 */
@Component
public class InMemoryResetTokenRepository implements ResetTokenRepository {

    /**
     * Key: Token Hash.
     */
    private final Cache<String, ResetToken> tokens;

    public InMemoryResetTokenRepository() {
        this.tokens = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofDays(1))
            .maximumSize(10000)
            .build();
    }

    @Override
    public Mono<Void> save(ResetToken resetToken) {
        return Mono.defer(() -> {
            var savedResetToken = tokens.get(resetToken.tokenHash(), k -> resetToken);
            if (Objects.equals(savedResetToken, resetToken)) {
                return Mono.empty();
            }
            // should never happen
            return Mono.error(new DuplicateKeyException("Reset token already exists"));
        });
    }

    @Override
    public Mono<ResetToken> findByTokenHash(String tokenHash) {
        return Mono.fromSupplier(() -> tokens.getIfPresent(tokenHash));
    }

    @Override
    public Mono<Void> removeByTokenHash(String tokenHash) {
        return Mono.fromRunnable(() -> tokens.invalidate(tokenHash));
    }

}
