package run.halo.app.security.sudo;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.GroupVersion;
import run.halo.app.notification.NotificationCenter;
import run.halo.app.notification.NotificationReasonEmitter;
import run.halo.app.notification.UserIdentity;

/**
 * Email one-time code sudo confirmation.
 *
 * @author johnniang
 * @since 2.27.0
 */
@Slf4j
@Component
@Order(1)
class EmailSudoVerificationProvider implements SudoVerificationProvider {

    static final String METHOD = "email";
    static final String SUDO_EMAIL_CODE_REASON_TYPE = "sudo-email-code";

    private final InMemorySudoCodeStore codeStore;
    private final NotificationReasonEmitter reasonEmitter;
    private final NotificationCenter notificationCenter;

    EmailSudoVerificationProvider(
            InMemorySudoCodeStore codeStore,
            NotificationReasonEmitter reasonEmitter,
            NotificationCenter notificationCenter) {
        this.codeStore = codeStore;
        this.reasonEmitter = reasonEmitter;
        this.notificationCenter = notificationCenter;
    }

    @Override
    public String method() {
        return METHOD;
    }

    @Override
    public boolean sendable() {
        return true;
    }

    @Override
    public Mono<Boolean> supports(User user) {
        var spec = user.getSpec();
        return Mono.just(spec.isEmailVerified() && StringUtils.isNotBlank(spec.getEmail()));
    }

    @Override
    public String maskedTarget(User user) {
        return maskEmail(user.getSpec().getEmail());
    }

    @Override
    public Mono<Void> sendCode(User user) {
        var username = user.getMetadata().getName();
        var email = user.getSpec().getEmail();
        return codeStore.generate(storeKey(username)).flatMap(code -> sendNotification(username, email, code));
    }

    @Override
    public Mono<Void> verify(User user, String code) {
        return codeStore.verify(storeKey(user.getMetadata().getName()), code);
    }

    Mono<Void> sendNotification(String username, String email, String code) {
        var subscribeNotification = autoSubscribe(email);
        var interestReasonSubject = createInterestReason(email).getSubject();
        var emitReasonMono = reasonEmitter.emit(
                SUDO_EMAIL_CODE_REASON_TYPE,
                builder -> builder.attribute("code", code)
                        .attribute("expirationAtMinutes", InMemorySudoCodeStore.CODE_TTL.toMinutes())
                        .attribute("username", username)
                        .author(UserIdentity.of(username))
                        .subject(Reason.Subject.builder()
                                .apiVersion(interestReasonSubject.getApiVersion())
                                .kind(interestReasonSubject.getKind())
                                .name(interestReasonSubject.getName())
                                .title("安全确认验证码：" + email)
                                .build()));
        return Mono.when(subscribeNotification)
                .then(emitReasonMono)
                .doOnSuccess(unused -> log.info("Sent sudo email code for user '{}'", username));
    }

    Mono<Void> autoSubscribe(String email) {
        var subscriber = new Subscription.Subscriber();
        subscriber.setName(UserIdentity.anonymousWithEmail(email).name());
        return notificationCenter
                .subscribe(subscriber, createInterestReason(email))
                .then();
    }

    Subscription.InterestReason createInterestReason(String email) {
        var interestReason = new Subscription.InterestReason();
        interestReason.setReasonType(SUDO_EMAIL_CODE_REASON_TYPE);
        interestReason.setSubject(Subscription.ReasonSubject.builder()
                .apiVersion(new GroupVersion(User.GROUP, User.KIND).toString())
                .kind(User.KIND)
                .name(UserIdentity.anonymousWithEmail(email).name())
                .build());
        return interestReason;
    }

    static String storeKey(String username) {
        return METHOD + ":" + username;
    }

    static String maskEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return null;
        }
        var at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
