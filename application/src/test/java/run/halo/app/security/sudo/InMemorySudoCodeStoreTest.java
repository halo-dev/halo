package run.halo.app.security.sudo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import run.halo.app.infra.exception.RateLimitExceededException;

class InMemorySudoCodeStoreTest {

    InMemorySudoCodeStore store;

    Clock baseClock;

    @BeforeEach
    void setUp() {
        store = new InMemorySudoCodeStore();
        baseClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        store.setClock(baseClock);
    }

    @Test
    void shouldGenerateAndVerifyCode() {
        var code = store.generate("email:alice").block();
        assertThat(code).hasSize(6);
        StepVerifier.create(store.verify("email:alice", code)).verifyComplete();
    }

    @Test
    void shouldFailWhenCodeIsWrong() {
        store.generate("email:alice").block();
        StepVerifier.create(store.verify("email:alice", "000000"))
                .expectError(SudoVerificationFailedException.class)
                .verify();
    }

    @Test
    void shouldFailWhenCodeExpired() {
        var code = store.generate("email:alice").block();
        store.setClock(Clock.offset(baseClock, InMemorySudoCodeStore.CODE_TTL.plusSeconds(1)));
        StepVerifier.create(store.verify("email:alice", code))
                .expectError(SudoVerificationFailedException.class)
                .verify();
    }

    @Test
    void shouldBlacklistAfterMaxAttempts() {
        store.generate("email:alice").block();
        for (int i = 0; i < InMemorySudoCodeStore.MAX_ATTEMPTS; i++) {
            StepVerifier.create(store.verify("email:alice", "000000"))
                    .expectError(SudoVerificationFailedException.class)
                    .verify();
        }
        StepVerifier.create(store.generate("email:alice"))
                .expectError(RateLimitExceededException.class)
                .verify();
    }

    @Test
    void shouldRateLimitResendWithinCooldown() {
        store.generate("email:alice").block();
        StepVerifier.create(store.generate("email:alice"))
                .expectError(RateLimitExceededException.class)
                .verify();
    }

    @Test
    void shouldAllowResendAfterCooldown() {
        var first = store.generate("email:alice").block();
        store.setClock(Clock.offset(baseClock, InMemorySudoCodeStore.RESEND_INTERVAL.plusSeconds(1)));
        var second = store.generate("email:alice").block();
        assertThat(second).isNotEqualTo(first);
        StepVerifier.create(store.verify("email:alice", first))
                .expectError(SudoVerificationFailedException.class)
                .verify();
        StepVerifier.create(store.verify("email:alice", second)).verifyComplete();
    }

    @Test
    void shouldNotReuseVerifiedCode() {
        var code = store.generate("email:alice").block();
        StepVerifier.create(store.verify("email:alice", code)).verifyComplete();
        StepVerifier.create(store.verify("email:alice", code))
                .expectError(SudoVerificationFailedException.class)
                .verify();
    }

    @Test
    void shouldAllowGenerateAfterCodeExpired() {
        store.generate("email:alice").block();
        store.setClock(Clock.offset(baseClock, Duration.ofMinutes(11)));
        StepVerifier.create(store.generate("email:alice")).expectNextCount(1).verifyComplete();
    }
}
