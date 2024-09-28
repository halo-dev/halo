package run.halo.app.core.user.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.core.user.service.EmailVerificationService;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.QueryFactory;
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
    public Mono<Void> sendVerificationCode(String username, String email) {
        Assert.state(StringUtils.isNotBlank(username), "Username must not be blank");
        Assert.state(StringUtils.isNotBlank(email), "Email must not be blank");
        return Mono.defer(() -> client.get(User.class, username)
                .flatMap(user -> {
                    var userEmail = user.getSpec().getEmail();
                    var isVerified = user.getSpec().isEmailVerified();
                    if (StringUtils.equals(userEmail, email) && isVerified) {
                        return Mono.error(
                            () -> new ServerWebInputException("Email already verified."));
                    }
                    var annotations = MetadataUtil.nullSafeAnnotations(user);
                    var oldEmailToVerify = annotations.get(User.EMAIL_TO_VERIFY);
                    var unsubMono = unSubscribeVerificationEmailNotification(oldEmailToVerify);
                    var updateUserAnnoMono = Mono.defer(() -> {
                        annotations.put(User.EMAIL_TO_VERIFY, email);
                        return client.update(user);
                    });
                    emailVerificationManager.removeCode(username, oldEmailToVerify);
                    return Mono.when(unsubMono, updateUserAnnoMono).thenReturn(user);
                })
            )
            .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance))
            .flatMap(user -> sendVerificationNotification(username, email));
    }

    @Override
    public Mono<Void> verify(String username, String code) {
        Assert.state(StringUtils.isNotBlank(username), "Username must not be blank");
        Assert.state(StringUtils.isNotBlank(code), "Code must not be blank");
        return Mono.defer(() -> client.get(User.class, username)
                .flatMap(user -> verifyUserEmail(user, code))
            )
            .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance));
    }

    private Mono<Void> verifyUserEmail(User user, String code) {
        var username = user.getMetadata().getName();
        var annotations = MetadataUtil.nullSafeAnnotations(user);
        var emailToVerify = annotations.get(User.EMAIL_TO_VERIFY);

        if (StringUtils.isBlank(emailToVerify)) {
            return Mono.error(EmailVerificationFailed::new);
        }

        var verified = emailVerificationManager.verifyCode(username, emailToVerify, code);
        if (!verified) {
            return Mono.error(EmailVerificationFailed::new);
        }

        return isEmailInUse(username, emailToVerify)
            .flatMap(inUse -> {
                if (inUse) {
                    return Mono.error(new EmailVerificationFailed("Email already in use.",
                        null,
                        "problemDetail.user.email.verify.emailInUse",
                        null)
                    );
                }
                // remove code when verified
                emailVerificationManager.removeCode(username, emailToVerify);
                user.getSpec().setEmailVerified(true);
                user.getSpec().setEmail(emailToVerify);
                return client.update(user);
            })
            .then();
    }

    Mono<Boolean> isEmailInUse(String username, String emailToVerify) {
        var listOptions = ListOptions.builder()
            .andQuery(QueryFactory.equal("spec.email", emailToVerify))
            .build();
        return client.listAll(User.class, listOptions, ExtensionUtil.defaultSort())
            .filter(user -> user.getSpec().isEmailVerified())
            .filter(user -> !user.getMetadata().getName().equals(username))
            .hasElements();
    }

    @Override
    public Mono<Void> sendRegisterVerificationCode(String email) {
        Assert.state(StringUtils.isNotBlank(email), "Email must not be blank");
        return sendVerificationNotification(email, email);
    }

    @Override
    public Mono<Boolean> verifyRegisterVerificationCode(String email, String code) {
        Assert.state(StringUtils.isNotBlank(email), "Username must not be blank");
        Assert.state(StringUtils.isNotBlank(code), "Code must not be blank");
        return Mono.fromSupplier(() -> emailVerificationManager.verifyCode(email, email, code))
            // Why use boundedElastic? Because the verification uses synchronized block.
            .subscribeOn(Schedulers.boundedElastic());
    }

    Mono<Void> sendVerificationNotification(String username, String email) {
        var code = emailVerificationManager.generateCode(username, email);
        var subscribeNotification = autoSubscribeVerificationEmailNotification(email);
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

    Mono<Void> autoSubscribeVerificationEmailNotification(String email) {
        var subscriber = new Subscription.Subscriber();
        subscriber.setName(UserIdentity.anonymousWithEmail(email).name());
        var interestReason = createInterestReason(email);
        return notificationCenter.subscribe(subscriber, interestReason)
            .then();
    }

    Mono<Void> unSubscribeVerificationEmailNotification(String oldEmail) {
        if (StringUtils.isBlank(oldEmail)) {
            return Mono.empty();
        }
        var subscriber = new Subscription.Subscriber();
        subscriber.setName(UserIdentity.anonymousWithEmail(oldEmail).name());
        return notificationCenter.unsubscribe(subscriber,
            createInterestReason(oldEmail));
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
