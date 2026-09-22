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

/**
 * In-memory one-time code store for sudo confirmation.
 *
 * <p>The store keeps only the generated code and its creation time, enforces the TTL manually, and consumes a code as
 * soon as it is verified successfully. Expired entries are pruned on the next generation, so the map stays bounded by
 * the number of users who asked for a code within the TTL.
 *
 * <p>Attempt limiting and resend throttling are deliberately <strong>not</strong> implemented here. Both are enforced
 * uniformly for every sudo method (TOTP and email alike) by the rate limiters owned by {@link SudoService}, so all
 * methods share one policy instead of email alone being locked out for an hour.
 *
 * <p>Like the login email code store, this store lives in the JVM. A deployment with more than one backend instance
 * therefore needs sticky sessions for sudo confirmation, and the confirm rate limiter is per instance as well.
 *
 * @author johnniang
 * @since 2.27.0
 */
@Component
class InMemorySudoCodeStore {

    static final int CODE_LENGTH = 6;
    static final Duration CODE_TTL = Duration.ofMinutes(10);

    private final ConcurrentHashMap<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

    private Clock clock = Clock.systemUTC();

    void setClock(Clock clock) {
        Assert.notNull(clock, "Clock must not be null");
        this.clock = clock;
    }

    Mono<String> generate(String key) {
        Assert.state(StringUtils.isNotBlank(key), "Key must not be blank");
        return Mono.fromCallable(() -> generateCode(key)).subscribeOn(Schedulers.boundedElastic());
    }

    Mono<Void> verify(String key, String code) {
        Assert.state(StringUtils.isNotBlank(key), "Key must not be blank");
        Assert.state(StringUtils.isNotBlank(code), "Code must not be blank");
        return Mono.fromRunnable(() -> verifyCode(key, code));
    }

    private String generateCode(String key) {
        codeStore.values().removeIf(entry -> entry.isExpired(clock));
        var code = RandomStringUtils.secure().nextNumeric(CODE_LENGTH);
        codeStore.put(key, new CodeEntry(code, clock.instant()));
        return code;
    }

    private void verifyCode(String key, String code) {
        var entry = codeStore.get(key);
        if (entry == null || entry.isExpired(clock) || !entry.matches(code)) {
            throw new SudoVerificationFailedException();
        }
        // A code is single-use: only the verification that wins the removal may succeed.
        if (!codeStore.remove(key, entry)) {
            throw new SudoVerificationFailedException();
        }
    }

    record CodeEntry(String code, Instant createdAt) {

        boolean isExpired(Clock clock) {
            return createdAt.plus(CODE_TTL).isBefore(clock.instant());
        }

        boolean matches(String submittedCode) {
            return MessageDigest.isEqual(
                    code.getBytes(StandardCharsets.UTF_8), submittedCode.getBytes(StandardCharsets.UTF_8));
        }
    }
}
