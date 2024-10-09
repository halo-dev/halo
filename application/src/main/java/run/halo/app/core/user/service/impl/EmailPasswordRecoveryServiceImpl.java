package run.halo.app.core.user.service.impl;

import java.time.Clock;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.core.user.service.EmailPasswordRecoveryService;
import run.halo.app.core.user.service.InvalidResetTokenException;
import run.halo.app.core.user.service.ResetToken;
import run.halo.app.core.user.service.ResetTokenRepository;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ExternalLinkProcessor;
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
    private static final Duration RESET_TOKEN_LIFE_TIME =
        Duration.ofMinutes(LINK_EXPIRATION_MINUTES);
    static final String RESET_PASSWORD_BY_EMAIL_REASON_TYPE = "reset-password-by-email";

    private final ExternalLinkProcessor externalLinkProcessor;
    private final ReactiveExtensionClient client;
    private final NotificationReasonEmitter reasonEmitter;
    private final NotificationCenter notificationCenter;
    private final UserService userService;
    private final ResetTokenRepository resetTokenRepository;

    private Clock clock = Clock.systemDefaultZone();

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
    public Mono<Void> sendPasswordResetEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return Mono.empty();
        }
        return userService.listByEmail(email)
            .filter(user -> user.getSpec().isEmailVerified())
            .next()
            .flatMap(user -> sendResetPasswordNotification(user.getMetadata().getName(), email));
    }

    @Override
    public Mono<Void> changePassword(String newPassword, String token) {
        Assert.state(StringUtils.isNotBlank(newPassword), "NewPassword must not be blank");
        Assert.state(StringUtils.isNotBlank(token), "Token for reset password must not be blank");
        var tokenHash = hashToken(token);
        return getValidResetToken(token).flatMap(resetToken ->
            userService.updateWithRawPassword(resetToken.username(), newPassword)
                .flatMap(user -> unSubscribeResetPasswordEmailNotification(
                    user.getSpec().getEmail())
                )
                .then(resetTokenRepository.removeByTokenHash(tokenHash))
        );
    }

    @Override
    public Mono<ResetToken> getValidResetToken(String token) {
        return resetTokenRepository.findByTokenHash(hashToken(token))
            .filter(resetToken -> clock.instant().isBefore(resetToken.expiresAt()))
            .switchIfEmpty(Mono.error(InvalidResetTokenException::new));
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

    private Mono<Void> sendResetPasswordNotification(String username, String email) {
        var token = generateToken();
        var tokenHash = hashToken(token);
        var expiresAt = clock.instant().plus(RESET_TOKEN_LIFE_TIME);
        var uri = UriComponentsBuilder.fromUriString("/")
            .pathSegment("password-reset", "email", token)
            .build(true)
            .toUri();
        var resetToken = new ResetToken(tokenHash, username, expiresAt);
        return resetTokenRepository.save(resetToken)
            .then(externalLinkProcessor.processLink(uri).flatMap(link -> {
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
                return autoSubscribeResetPasswordEmailNotification(email).then(emitReasonMono);
            }));
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

    private static String hashToken(String token) {
        return Sha512DigestUtils.shaHex(token);
    }

    private static String generateToken() {
        return RandomStringUtils.secure().nextAlphanumeric(64);
    }

}
