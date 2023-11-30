package run.halo.app.core.extension.service.impl;

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
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.core.extension.service.EmailPasswordRecoveryService;
import run.halo.app.core.extension.service.UserService;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ExternalLinkProcessor;
import run.halo.app.infra.exception.AccessDeniedException;
import run.halo.app.infra.exception.RateLimitExceededException;
import run.halo.app.notification.NotificationCenter;
import run.halo.app.notification.NotificationReasonEmitter;
import run.halo.app.notification.UserIdentity;

/**
 * A default implementation for {@link EmailPasswordRecoveryService}.
 *
 * @author guqing
 * @since 2.11.0
 */
@Component
@RequiredArgsConstructor
public class EmailPasswordRecoveryServiceImpl implements EmailPasswordRecoveryService {
    public static final int MAX_ATTEMPTS = 5;
    public static final long LINK_EXPIRATION_MINUTES = 30;
    static final String RESET_PASSWORD_BY_EMAIL_REASON_TYPE = "reset-password-by-email";

    private final ResetPasswordVerificationManager resetPasswordVerificationManager =
        new ResetPasswordVerificationManager();
    private final ExternalLinkProcessor externalLinkProcessor;
    private final ReactiveExtensionClient client;
    private final NotificationReasonEmitter reasonEmitter;
    private final NotificationCenter notificationCenter;
    private final UserService userService;

    @Override
    public Mono<Void> sendPasswordResetEmail(String username, String email) {
        return client.fetch(User.class, username)
            .flatMap(user -> {
                var userEmail = user.getSpec().getEmail();
                if (!StringUtils.equals(userEmail, email)) {
                    return Mono.empty();
                }
                if (!user.getSpec().isEmailVerified()) {
                    return Mono.empty();
                }
                return sendResetPasswordNotification(username, email);
            });
    }

    @Override
    public Mono<Void> changePassword(String username, String newPassword, String token) {
        Assert.state(StringUtils.isNotBlank(username), "Username must not be blank");
        Assert.state(StringUtils.isNotBlank(newPassword), "NewPassword must not be blank");
        Assert.state(StringUtils.isNotBlank(token), "Token for reset password must not be blank");
        var verified = resetPasswordVerificationManager.verifyToken(username, token);
        if (!verified) {
            return Mono.error(AccessDeniedException::new);
        }
        return userService.updateWithRawPassword(username, newPassword)
            .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance))
            .flatMap(user -> {
                resetPasswordVerificationManager.removeToken(username);
                return unSubscribeResetPasswordEmailNotification(user.getSpec().getEmail());
            })
            .then();
    }

    Mono<Void> unSubscribeResetPasswordEmailNotification(String email) {
        if (StringUtils.isBlank(email)) {
            return Mono.empty();
        }
        var subscriber = new Subscription.Subscriber();
        subscriber.setName(UserIdentity.anonymousWithEmail(email).name());
        return notificationCenter.unsubscribe(subscriber, createInterestReason(email))
            .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance));
    }

    Mono<Void> sendResetPasswordNotification(String username, String email) {
        var token = resetPasswordVerificationManager.generateToken(username);
        var link = getResetPasswordLink(username, token);

        var subscribeNotification = autoSubscribeResetPasswordEmailNotification(email);
        var interestReasonSubject = createInterestReason(email).getSubject();
        var emitReasonMono = reasonEmitter.emit(RESET_PASSWORD_BY_EMAIL_REASON_TYPE,
            builder -> builder.attribute("expirationAtMinutes", LINK_EXPIRATION_MINUTES)
                .attribute("username", username)
                .attribute("link", link)
                .author(UserIdentity.of(username))
                .subject(Reason.Subject.builder()
                    .apiVersion(interestReasonSubject.getApiVersion())
                    .kind(interestReasonSubject.getKind())
                    .name(interestReasonSubject.getName())
                    .title("使用邮箱地址重置密码：" + email)
                    .build()
                )
        );
        return Mono.when(subscribeNotification).then(emitReasonMono);
    }

    Mono<Void> autoSubscribeResetPasswordEmailNotification(String email) {
        var subscriber = new Subscription.Subscriber();
        subscriber.setName(UserIdentity.anonymousWithEmail(email).name());
        var interestReason = createInterestReason(email);
        return notificationCenter.subscribe(subscriber, interestReason)
            .then();
    }

    Subscription.InterestReason createInterestReason(String email) {
        var interestReason = new Subscription.InterestReason();
        interestReason.setReasonType(RESET_PASSWORD_BY_EMAIL_REASON_TYPE);
        interestReason.setSubject(Subscription.ReasonSubject.builder()
            .apiVersion(new GroupVersion(User.GROUP, User.KIND).toString())
            .kind(User.KIND)
            .name(UserIdentity.anonymousWithEmail(email).name())
            .build());
        return interestReason;
    }

    private String getResetPasswordLink(String username, String token) {
        return externalLinkProcessor.processLink(
            "/uc/reset-password/" + username + "?reset_password_token=" + token);
    }

    static class ResetPasswordVerificationManager {
        private final Cache<String, Verification> userTokenCache =
            CacheBuilder.newBuilder()
                .expireAfterWrite(LINK_EXPIRATION_MINUTES, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();

        private final Cache<String, Boolean>
            blackListCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofHours(2))
            .maximumSize(1000)
            .build();

        public boolean verifyToken(String username, String token) {
            var verification = userTokenCache.getIfPresent(username);
            if (verification == null) {
                // expired or not generated
                return false;
            }
            if (blackListCache.getIfPresent(username) != null) {
                // in blacklist
                throw new RateLimitExceededException(null);
            }
            synchronized (verification) {
                if (verification.getAttempts().get() >= MAX_ATTEMPTS) {
                    // add to blacklist to prevent brute force attack
                    blackListCache.put(username, true);
                    return false;
                }
                if (!verification.getToken().equals(token)) {
                    verification.getAttempts().incrementAndGet();
                    return false;
                }
            }
            return true;
        }

        public void removeToken(String username) {
            userTokenCache.invalidate(username);
        }

        public String generateToken(String username) {
            Assert.state(StringUtils.isNotBlank(username), "Username must not be blank");
            var verification = new Verification();
            verification.setToken(RandomStringUtils.randomAlphanumeric(20));
            verification.setAttempts(new AtomicInteger(0));
            userTokenCache.put(username, verification);
            return verification.getToken();
        }

        /**
         * Only for test.
         */
        boolean contains(String username) {
            return userTokenCache.getIfPresent(username) != null;
        }

        @Data
        @Accessors(chain = true)
        static class Verification {
            private String token;
            private AtomicInteger attempts;
        }
    }
}
