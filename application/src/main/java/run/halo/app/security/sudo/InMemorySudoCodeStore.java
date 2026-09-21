package run.halo.app.security.sudo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.infra.exception.RateLimitExceededException;

/**
 * In-memory one-time code store for sudo confirmation.
 *
 * <p>Entries use manual TTL, attempt tracking, per-key blacklisting, and a resend cooldown. All read-modify-write
 * operations use CAS retries so the store is safe on WebFlux event-loop threads.
 *
 * @author johnniang
 * @since 2.27.0
 */
@Component
class InMemorySudoCodeStore {

    static final int CODE_LENGTH = 6;
    static final int MAX_ATTEMPTS = 5;
    static final Duration CODE_TTL = Duration.ofMinutes(10);
    static final Duration BLACKLIST_TTL = Duration.ofHours(1);
    static final Duration RESEND_INTERVAL = Duration.ofMinutes(1);

    private final ConcurrentHashMap<String, CodeEntry> codeStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> blacklistStore = new ConcurrentHashMap<>();

    private Clock clock = Clock.systemUTC();

    void setClock(Clock clock) {
        Assert.notNull(clock, "Clock must not be null");
        this.clock = clock;
    }

    Mono<String> generate(String key) {
        Assert.state(StringUtils.isNotBlank(key), "Key must not be blank");
        return Mono.fromCallable(() -> generateAtomically(key)).subscribeOn(Schedulers.boundedElastic());
    }

    Mono<Void> verify(String key, String code) {
        Assert.state(StringUtils.isNotBlank(key), "Key must not be blank");
        Assert.state(StringUtils.isNotBlank(code), "Code must not be blank");
        return Mono.fromCallable(() -> verifyAtomically(key, code)).flatMap(outcome -> switch (outcome) {
            case SUCCESS -> Mono.empty();
            case WRONG_CODE, EXPIRED, BLACKLISTED -> Mono.error(SudoVerificationFailedException::new);
        });
    }

    private String generateAtomically(String key) {
        if (isBlacklisted(key)) {
            throw new RateLimitExceededException(null);
        }
        var current = codeStore.get(key);
        if (current != null && !current.isExpired(clock) && current.isInResendCooldown(clock)) {
            throw new RateLimitExceededException(null);
        }
        var code = RandomStringUtils.secure().nextNumeric(CODE_LENGTH);
        codeStore.put(key, new CodeEntry(code, 0, clock.instant()));
        return code;
    }

    private VerifyOutcome verifyAtomically(String key, String submittedCode) {
        if (isBlacklisted(key)) {
            return VerifyOutcome.BLACKLISTED;
        }
        for (int retries = 0; retries < 10; retries++) {
            var current = codeStore.get(key);
            if (current == null || current.isExpired(clock)) {
                codeStore.remove(key, current);
                return VerifyOutcome.EXPIRED;
            }
            if (current.isBlacklisted()) {
                codeStore.remove(key, current);
                blacklistStore.put(key, clock.instant());
                return VerifyOutcome.BLACKLISTED;
            }
            if (!codesEqual(current.code(), submittedCode)) {
                var updated = current.withIncrementedAttempts();
                if (codeStore.replace(key, current, updated)) {
                    if (updated.isBlacklisted()) {
                        blacklistStore.put(key, clock.instant());
                        codeStore.remove(key, updated);
                        return VerifyOutcome.BLACKLISTED;
                    }
                    return VerifyOutcome.WRONG_CODE;
                }
                continue;
            }
            if (codeStore.remove(key, current)) {
                blacklistStore.remove(key);
                return VerifyOutcome.SUCCESS;
            }
        }
        return VerifyOutcome.EXPIRED;
    }

    private boolean isBlacklisted(String key) {
        var blacklistedAt = blacklistStore.get(key);
        return blacklistedAt != null && blacklistedAt.plus(BLACKLIST_TTL).isAfter(clock.instant());
    }

    private static boolean codesEqual(String expected, String submitted) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), submitted.getBytes(StandardCharsets.UTF_8));
    }

    enum VerifyOutcome {
        SUCCESS,
        WRONG_CODE,
        EXPIRED,
        BLACKLISTED
    }

    record CodeEntry(String code, int attempts, Instant createdAt) {

        boolean isExpired(Clock clock) {
            return createdAt.plus(CODE_TTL).isBefore(clock.instant());
        }

        boolean isBlacklisted() {
            return attempts >= MAX_ATTEMPTS;
        }

        boolean isInResendCooldown(Clock clock) {
            return createdAt.plus(RESEND_INTERVAL).isAfter(clock.instant());
        }

        CodeEntry withIncrementedAttempts() {
            return new CodeEntry(code, attempts + 1, createdAt);
        }
    }
}
