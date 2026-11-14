package run.halo.app.security.sudo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

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
    void shouldFailWhenNoCodeWasGenerated() {
        StepVerifier.create(store.verify("email:alice", "000000"))
                .expectError(SudoVerificationFailedException.class)
                .verify();
    }

    @Test
    void shouldFailWhenCodeIsWrong() {
        store.generate("email:alice").block();
        StepVerifier.create(store.verify("email:alice", "000000"))
                .expectError(SudoVerificationFailedException.class)
                .verify();
    }

    @Test
    void shouldKeepCodeUsableAfterWrongAttempt() {
        // Attempt limiting is owned by the SudoService rate limiter, not by the store.
        var code = store.generate("email:alice").block();
        StepVerifier.create(store.verify("email:alice", "000000"))
                .expectError(SudoVerificationFailedException.class)
                .verify();
        StepVerifier.create(store.verify("email:alice", code)).verifyComplete();
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
    void shouldNotReuseVerifiedCode() {
        var code = store.generate("email:alice").block();
        StepVerifier.create(store.verify("email:alice", code)).verifyComplete();
        StepVerifier.create(store.verify("email:alice", code))
                .expectError(SudoVerificationFailedException.class)
                .verify();
    }

    @Test
    void shouldInvalidatePreviousCodeWhenRegenerated() {
        var first = store.generate("email:alice").block();
        var second = store.generate("email:alice").block();
        StepVerifier.create(store.verify("email:alice", first))
                .expectError(SudoVerificationFailedException.class)
                .verify();
        StepVerifier.create(store.verify("email:alice", second)).verifyComplete();
    }

    @Test
    void shouldAllowGenerateAfterCodeExpired() {
        store.generate("email:alice").block();
        store.setClock(Clock.offset(baseClock, InMemorySudoCodeStore.CODE_TTL.plusSeconds(1)));
        StepVerifier.create(store.generate("email:alice")).expectNextCount(1).verifyComplete();
    }
}
