package run.halo.app.security.authentication.emailcode;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
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
 * Default implementation of {@link EmailCodeService} that manages login verification codes in memory and sends them via
 * the notification system.
 *
 * @author johnniang
 * @since 2.26.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailCodeServiceImpl implements EmailCodeService {

    static final int MAX_ATTEMPTS = 3;
    static final long CODE_EXPIRATION_MINUTES = 5;
    static final String LOGIN_EMAIL_CODE_REASON_TYPE = "login-email-code";

    private final LoginCodeManager loginCodeManager = new LoginCodeManager();
    private final UserService userService;
    private final NotificationReasonEmitter reasonEmitter;
    private final NotificationCenter notificationCenter;

    @Override
    public Mono<Void> sendLoginCode(String email) {
        Assert.state(StringUtils.isNotBlank(email), "Email must not be blank");
        return userService
                .listByEmail(email)
                .filter(user -> user.getSpec().isEmailVerified())
                .next()
                .flatMap(user -> {
                    var username = user.getMetadata().getName();
                    return sendLoginCodeNotification(username, email);
                });
    }

    @Override
    public Mono<User> verifyLoginCode(String email, String code) {
        Assert.state(StringUtils.isNotBlank(email), "Email must not be blank");
        Assert.state(StringUtils.isNotBlank(code), "Code must not be blank");
        return Mono.fromSupplier(() -> {
                    var key = email.toLowerCase();
                    var verified = loginCodeManager.verifyCode(key, code);
                    if (!verified) {
                        throw new BadCredentialsException("Invalid or expired verification code");
                    }
                    loginCodeManager.removeCode(key);
                    return key;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(key -> userService
                        .listByEmail(email)
                        .filter(user -> user.getSpec().isEmailVerified())
                        .next()
                        .switchIfEmpty(
                                Mono.error(() -> new BadCredentialsException("Invalid email or user not found"))));
    }

    Mono<Void> sendLoginCodeNotification(String username, String email) {
        var code = loginCodeManager.generateCode(email.toLowerCase());
        if (log.isDebugEnabled()) {
            log.debug("Generated login code for email '{}': {}", email, code);
        }
        var subscribeNotification = autoSubscribeLoginCodeNotification(email);
        var interestReasonSubject = createInterestReason(email).getSubject();
        var emitReasonMono = reasonEmitter.emit(
                LOGIN_EMAIL_CODE_REASON_TYPE,
                builder -> builder.attribute("code", code)
                        .attribute("expirationAtMinutes", CODE_EXPIRATION_MINUTES)
                        .attribute("username", username)
                        .author(UserIdentity.of(username))
                        .subject(Reason.Subject.builder()
                                .apiVersion(interestReasonSubject.getApiVersion())
                                .kind(interestReasonSubject.getKind())
                                .name(interestReasonSubject.getName())
                                .title("登录验证码：" + email)
                                .build()));
        return Mono.when(subscribeNotification).then(emitReasonMono);
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

    /** In-memory login code manager with expiry, attempt limiting, and blacklisting. */
    static class LoginCodeManager {
        private final Cache<String, Verification> codeCache = CacheBuilder.newBuilder()
                .expireAfterWrite(CODE_EXPIRATION_MINUTES, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();

        private final Cache<String, Boolean> blackListCache = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofHours(1))
                .maximumSize(1000)
                .build();

        boolean verifyCode(String key, String code) {
            var verification = codeCache.getIfPresent(key);
            if (verification == null) {
                return false;
            }
            if (blackListCache.getIfPresent(key) != null) {
                return false;
            }
            synchronized (verification) {
                if (verification.getAttempts().get() >= MAX_ATTEMPTS) {
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

        void removeCode(String key) {
            codeCache.invalidate(key);
        }

        String generateCode(String key) {
            Assert.state(StringUtils.isNotBlank(key), "Key must not be blank");
            var verification = new Verification();
            verification.setCode(RandomStringUtils.randomNumeric(6));
            verification.setAttempts(new AtomicInteger(0));
            codeCache.put(key, verification);
            return verification.getCode();
        }

        @Data
        @Accessors(chain = true)
        static class Verification {
            private String code;
            private AtomicInteger attempts;
        }
    }
}
