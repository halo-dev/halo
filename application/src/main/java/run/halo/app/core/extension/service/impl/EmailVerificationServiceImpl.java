package run.halo.app.core.extension.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.core.extension.service.EmailVerificationService;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.EmailVerificationFailed;
import run.halo.app.notification.NotificationCenter;
import run.halo.app.notification.NotificationReasonEmitter;
import run.halo.app.notification.UserIdentity;

/**
 * A default implementation of {@link EmailVerificationService}.
 *
 * @author guqing
 * @since 2.11.0
 */
@Component
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {
    public static final int MAX_ATTEMPTS = 5;
    public static final long CODE_EXPIRATION_MINUTES = 10;
    static final String EMAIL_VERIFICATION_REASON_TYPE = "email-verification";

    private final EmailVerificationManager emailVerificationManager =
        new EmailVerificationManager();
    private final ReactiveExtensionClient client;
    private final NotificationReasonEmitter reasonEmitter;
    private final NotificationCenter notificationCenter;

    @Override
    public Mono<Void> sendVerificationCode(String username) {
        return client.get(User.class, username)
            .flatMap(user -> {
                var email = user.getSpec().getEmail();
                if (StringUtils.isBlank(email)) {
                    return Mono.error(
                        () -> new ServerWebInputException("Email must not be blank."));
                }
                if (user.getSpec().isEmailVerified()) {
                    return Mono.error(() -> new ServerWebInputException("Email already verified."));
                }
                return sendVerificationNotification(user);
            });
    }

    @Override
    public Mono<Void> verify(String username, String code) {
        return Mono.defer(() -> client.get(User.class, username)
                .flatMap(user -> {
                    var email = user.getSpec().getEmail();
                    var verified = emailVerificationManager.verifyCode(username, email, code);
                    if (!verified) {
                        return Mono.error(EmailVerificationFailed::new);
                    }
                    user.getSpec().setEmailVerified(true);
                    emailVerificationManager.removeCode(username, email);
                    return client.update(user);
                })
            )
            .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance))
            .then();
    }

    Mono<Void> sendVerificationNotification(User user) {
        var email = user.getSpec().getEmail();
        var username = user.getMetadata().getName();
        var oldEmail = MetadataUtil.nullSafeAnnotations(user)
            .get(User.LAST_USED_EMAIL_ANNO);
        var code = emailVerificationManager.generateCode(username, email);
        var subscribeNotification = autoSubscribeVerificationEmail(email, oldEmail);
        var interestReasonSubject = createInterestReason(email).getSubject();
        var emitReasonMono = reasonEmitter.emit(EMAIL_VERIFICATION_REASON_TYPE,
            builder -> builder.attribute("code", code)
                .attribute("expirationAtMinutes", CODE_EXPIRATION_MINUTES)
                .attribute("username", username)
                .author(UserIdentity.of(username))
                .subject(Reason.Subject.builder()
                    .apiVersion(interestReasonSubject.getApiVersion())
                    .kind(interestReasonSubject.getKind())
                    .name(interestReasonSubject.getName())
                    .title("验证邮箱：" + email)
                    .build()
                )
        );
        return Mono.when(subscribeNotification).then(emitReasonMono);
    }

    Mono<Void> autoSubscribeVerificationEmail(String email, String oldEmail) {
        List<Publisher<?>> publishers = new ArrayList<>(2);
        if (StringUtils.isNotBlank(oldEmail) && !oldEmail.equals(email)) {
            var subscriber = new Subscription.Subscriber();
            subscriber.setName(UserIdentity.anonymousWithEmail(oldEmail).name());
            var unsubMono = notificationCenter.unsubscribe(subscriber,
                createInterestReason(oldEmail));
            publishers.add(unsubMono);
        }
        var subscriber = new Subscription.Subscriber();
        subscriber.setName(UserIdentity.anonymousWithEmail(email).name());
        var interestReason = createInterestReason(email);
        var subMono = notificationCenter.subscribe(subscriber, interestReason);
        publishers.add(subMono);
        return Mono.when(publishers);
    }

    Subscription.InterestReason createInterestReason(String email) {
        var interestReason = new Subscription.InterestReason();
        interestReason.setReasonType(EMAIL_VERIFICATION_REASON_TYPE);
        interestReason.setSubject(Subscription.ReasonSubject.builder()
            .apiVersion(new GroupVersion(User.GROUP, User.KIND).toString())
            .kind(User.KIND)
            .name(UserIdentity.anonymousWithEmail(email).name())
            .build());
        return interestReason;
    }

    /**
     * A simple email verification manager that stores the verification code in memory.
     * It is a thread-safe class.
     *
     * @author guqing
     * @since 2.11.0
     */
    static class EmailVerificationManager {
        private final Cache<UsernameEmail, Verification> emailVerificationCodeCache =
            CacheBuilder.newBuilder()
                .expireAfterWrite(CODE_EXPIRATION_MINUTES, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();

        private final Cache<UsernameEmail, Boolean> blackListCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofHours(1))
            .maximumSize(1000)
            .build();

        public boolean verifyCode(String username, String email, String code) {
            var key = new UsernameEmail(username, email);
            var verification = emailVerificationCodeCache.getIfPresent(key);
            if (verification == null) {
                // expired or not generated
                return false;
            }
            if (blackListCache.getIfPresent(key) != null) {
                // in blacklist
                throw new EmailVerificationFailed("Too many attempts. Please try again later.",
                    null,
                    "problemDetail.user.email.verify.maxAttempts",
                    null);
            }
            synchronized (verification) {
                if (verification.getAttempts().get() >= MAX_ATTEMPTS) {
                    // add to blacklist to prevent brute force attack
                    blackListCache.put(key, true);
                    return false;
                }
                if (!verification.getCode().equals(code)) {
                    verification.getAttempts().incrementAndGet();
                    return false;
                }
            }
            return true;
        }

        public void removeCode(String username, String email) {
            var key = new UsernameEmail(username, email);
            emailVerificationCodeCache.invalidate(key);
        }

        public String generateCode(String username, String email) {
            Assert.state(StringUtils.isNotBlank(username), "Username must not be blank");
            Assert.state(StringUtils.isNotBlank(email), "Email must not be blank");
            var key = new UsernameEmail(username, email);
            var verification = new Verification();
            verification.setCode(RandomStringUtils.randomNumeric(6));
            verification.setAttempts(new AtomicInteger(0));
            emailVerificationCodeCache.put(key, verification);
            return verification.getCode();
        }

        /**
         * Only for test.
         */
        boolean contains(String username, String email) {
            return emailVerificationCodeCache
                .getIfPresent(new UsernameEmail(username, email)) != null;
        }

        record UsernameEmail(String username, String email) {
        }

        @Data
        @Accessors(chain = true)
        static class Verification {
            private String code;
            private AtomicInteger attempts;
        }
    }
}
