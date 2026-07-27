package run.halo.app.security.authentication.emailcode;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.GroupVersion;
import run.halo.app.notification.NotificationCenter;
import run.halo.app.notification.NotificationReasonEmitter;
import run.halo.app.notification.UserIdentity;

/**
 * In-memory implementation of {@link EmailCodeService} that manages login verification codes in a
 * {@link ConcurrentHashMap} and sends them through the notification system.
 *
 * <p>Code entries are stored with manual TTL enforcement (5 min), attempt tracking (max 3), and per-email blacklisting
 * (1 h). All read-modify-write operations use CAS-based retry loops to avoid thread blocking, making this
 * implementation safe for reactive (WebFlux) event-loop threads.
 *
 * <p>Code generation uses {@link RandomStringUtils#secure()} (SecureRandom via /dev/urandom) and is offloaded to
 * {@link Schedulers#boundedElastic()} to prevent any possibility of blocking the event loop.
 *
 * @author johnniang
 * @since 2.26.0
 */
@Slf4j
@Component
public class InMemoryEmailCodeService implements EmailCodeService {

    static final int CODE_LENGTH = 6;
    static final int MAX_ATTEMPTS = 3;
    static final Duration CODE_TTL = Duration.ofMinutes(5);
    static final Duration BLACKLIST_TTL = Duration.ofHours(1);
    static final String LOGIN_EMAIL_CODE_REASON_TYPE = "login-email-code";

    private final ConcurrentHashMap<String, LoginCodeEntry> codeStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> blacklistStore = new ConcurrentHashMap<>();

    private Clock clock = Clock.systemUTC();

    private final UserService userService;
    private final NotificationReasonEmitter reasonEmitter;
    private final NotificationCenter notificationCenter;

    public InMemoryEmailCodeService(
            UserService userService, NotificationReasonEmitter reasonEmitter, NotificationCenter notificationCenter) {
        this.userService = userService;
        this.reasonEmitter = reasonEmitter;
        this.notificationCenter = notificationCenter;
    }

    void setClock(Clock clock) {
        Assert.notNull(clock, "Clock must not be null");
        this.clock = clock;
    }

    @Override
    public Mono<Void> sendLoginCode(String email) {
        Assert.state(StringUtils.isNotBlank(email), "Email must not be blank");
        return userService
                .listByEmail(email)
                .filter(user -> user.getSpec().isEmailVerified())
                .next()
                .flatMap(user -> sendLoginCodeNotification(user.getMetadata().getName(), email));
    }

    @Override
    public Mono<User> verifyLoginCode(String email, String code) {
        Assert.state(StringUtils.isNotBlank(email), "Email must not be blank");
        Assert.state(StringUtils.isNotBlank(code), "Code must not be blank");
        var key = email.toLowerCase();

        return Mono.fromCallable(() -> verifyCodeAtomically(key, code)).flatMap(result -> switch (result.outcome()) {
            case SUCCESS ->
                userService
                        .listByEmail(email)
                        .filter(user -> user.getSpec().isEmailVerified())
                        .next()
                        .switchIfEmpty(
                                Mono.error(() -> new BadCredentialsException("Invalid email or user not found.")))
                        .doOnSuccess(user -> blacklistStore.remove(key));
            case WRONG_CODE -> Mono.error(new BadCredentialsException("Invalid verification code."));
            case EXPIRED -> Mono.error(new BadCredentialsException("Invalid or expired verification code."));
            case BLACKLISTED -> Mono.error(new BadCredentialsException("Too many attempts. Please try again later."));
        });
    }

    Mono<Void> sendLoginCodeNotification(String username, String email) {
        var key = email.toLowerCase();
        return Mono.fromCallable(() -> {
                    var code = RandomStringUtils.secure().nextNumeric(CODE_LENGTH);
                    log.debug("Generated login code for email '{}'", email);
                    var entry = new LoginCodeEntry(code, 0, clock.instant());
                    codeStore.put(key, entry);
                    return code;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(code -> {
                    var subscribeNotification = autoSubscribeLoginCodeNotification(email);
                    var interestReasonSubject = createInterestReason(email).getSubject();
                    var emitReasonMono = reasonEmitter.emit(
                            LOGIN_EMAIL_CODE_REASON_TYPE,
                            builder -> builder.attribute("code", code)
                                    .attribute("expirationAtMinutes", CODE_TTL.toMinutes())
                                    .attribute("username", username)
                                    .author(UserIdentity.of(username))
                                    .subject(Reason.Subject.builder()
                                            .apiVersion(interestReasonSubject.getApiVersion())
                                            .kind(interestReasonSubject.getKind())
                                            .name(interestReasonSubject.getName())
                                            .title("登录验证码：" + email)
                                            .build()));
                    return Mono.when(subscribeNotification).then(emitReasonMono);
                });
    }

    Mono<Void> autoSubscribeLoginCodeNotification(String email) {
        var subscriber = new Subscription.Subscriber();
        subscriber.setName(UserIdentity.anonymousWithEmail(email).name());
        var interestReason = createInterestReason(email);
        return notificationCenter.subscribe(subscriber, interestReason).then();
    }

    Subscription.InterestReason createInterestReason(String email) {
        var interestReason = new Subscription.InterestReason();
        interestReason.setReasonType(LOGIN_EMAIL_CODE_REASON_TYPE);
        interestReason.setSubject(Subscription.ReasonSubject.builder()
                .apiVersion(new GroupVersion(User.GROUP, User.KIND).toString())
                .kind(User.KIND)
                .name(UserIdentity.anonymousWithEmail(email).name())
                .build());
        return interestReason;
    }

    /**
     * Atomically verifies the submitted code using a CAS retry loop. All operations are on {@link ConcurrentHashMap}
     * which uses per-bin locking at most — no blocking on Caffeine internal locks.
     */
    private VerifyCodeResult verifyCodeAtomically(String key, String submittedCode) {
        // Check blacklist first
        var blacklistedAt = blacklistStore.get(key);
        if (blacklistedAt != null && blacklistedAt.plus(BLACKLIST_TTL).isAfter(clock.instant())) {
            return new VerifyCodeResult(VerifyOutcome.BLACKLISTED);
        }

        // CAS loop for atomic read-modify-write
        for (int retries = 0; retries < 10; retries++) {
            var current = codeStore.get(key);

            if (current == null || current.isExpired(clock)) {
                codeStore.remove(key, current);
                return new VerifyCodeResult(VerifyOutcome.EXPIRED);
            }

            if (current.isBlacklisted()) {
                codeStore.remove(key, current);
                blacklistStore.put(key, clock.instant());
                return new VerifyCodeResult(VerifyOutcome.BLACKLISTED);
            }

            if (!current.code().equals(submittedCode)) {
                var updated = current.withIncrementedAttempts();
                if (codeStore.replace(key, current, updated)) {
                    if (updated.isBlacklisted()) {
                        blacklistStore.put(key, clock.instant());
                        codeStore.remove(key, updated);
                        return new VerifyCodeResult(VerifyOutcome.BLACKLISTED);
                    }
                    return new VerifyCodeResult(VerifyOutcome.WRONG_CODE);
                }
                continue;
            }

            // Code matches — remove entry so it cannot be reused
            if (codeStore.remove(key, current)) {
                return new VerifyCodeResult(VerifyOutcome.SUCCESS);
            }
        }

        // Safety valve: fall back to EXPIRED if CAS loop doesn't converge
        return new VerifyCodeResult(VerifyOutcome.EXPIRED);
    }

    /** Verification outcome computed atomically inside the CAS loop. */
    enum VerifyOutcome {
        SUCCESS,
        WRONG_CODE,
        EXPIRED,
        BLACKLISTED
    }

    /** Wrapper for the outcome of an atomic verification attempt. */
    record VerifyCodeResult(VerifyOutcome outcome) {}

    /** Immutable entry representing a generated login code and its verification state. */
    record LoginCodeEntry(String code, int attempts, Instant createdAt) {

        boolean isExpired(Clock clock) {
            return createdAt.plus(CODE_TTL).isBefore(clock.instant());
        }

        boolean isBlacklisted() {
            return attempts >= MAX_ATTEMPTS;
        }

        LoginCodeEntry withIncrementedAttempts() {
            return new LoginCodeEntry(code, attempts + 1, createdAt);
        }
    }
}
