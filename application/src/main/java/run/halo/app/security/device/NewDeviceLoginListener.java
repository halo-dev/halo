package run.halo.app.security.device;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Device;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.notification.NotificationCenter;
import run.halo.app.notification.NotificationReasonEmitter;
import run.halo.app.notification.ReasonAttributes;
import run.halo.app.notification.UserIdentity;

/**
 * <p>Sends a notification when a new device login,It listens for {@link NewDeviceLoginEvent}
 * asynchronously.</p>
 *
 * @author guqing
 * @since 2.17.0
 */
@Component
@RequiredArgsConstructor
public class NewDeviceLoginListener implements ApplicationListener<NewDeviceLoginEvent> {
    static final String REASON_TYPE = "new-device-login";
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O").withZone(ZoneOffset.systemDefault());
    private final NotificationCenter notificationCenter;
    private final NotificationReasonEmitter notificationReasonEmitter;

    @Async
    @Override
    public void onApplicationEvent(@NonNull NewDeviceLoginEvent event) {
        subscribeForNewDeviceLoginReason(event.getDevice())
            .then(sendNewDeviceNotification(event.getDevice()))
            .block();
    }

    Mono<Void> sendNewDeviceNotification(Device device) {
        return notificationReasonEmitter.emit(REASON_TYPE, builder -> {
            var attributes = new ReasonAttributes();
            attributes.put("principalName", device.getSpec().getPrincipalName());
            attributes.put("os", device.getStatus().getOs());
            attributes.put("browser", device.getStatus().getBrowser());
            attributes.put("ipAddress", device.getSpec().getIpAddress());
            attributes.put("loginTime",
                DATE_TIME_FORMATTER.format(device.getSpec().getLastAuthenticatedTime()));
            builder.attributes(attributes)
                .author(UserIdentity.of(device.getSpec().getPrincipalName()))
                .subject(Reason.Subject.builder()
                    .apiVersion(Device.GROUP + "/" + Device.VERSION)
                    .kind(Device.KIND)
                    .name(device.getMetadata().getName())
                    .title("在新设备上登录")
                    .build());
        });
    }

    Mono<Void> subscribeForNewDeviceLoginReason(Device device) {
        var principalName = device.getSpec().getPrincipalName();
        var subscriber = new Subscription.Subscriber();
        subscriber.setName(principalName);

        var reason = new Subscription.InterestReason();
        reason.setReasonType(REASON_TYPE);
        reason.setExpression("props.principalName == '%s'".formatted(principalName));
        return notificationCenter.subscribe(subscriber, reason)
            .then();
    }
}
