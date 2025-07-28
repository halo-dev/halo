package run.halo.app.notification;

import java.time.Duration;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;

/**
 * <p>Notification trigger for {@link Reason}.</p>
 * <p>Triggered when a new {@link Reason} is received, and then notify through
 * {@link NotificationCenter}.</p>
 * <p>It will add a finalizer to the {@link Reason} to avoid duplicate notification, In other
 * words, it will only notify once.</p>
 *
 * @author guqing
 * @since 2.10.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationTrigger implements Reconciler<Reconciler.Request> {

    public static final String TRIGGERED_FINALIZER = "triggered";
    private static final Duration TIMEOUT = Duration.ofMinutes(1);

    private final ExtensionClient client;
    private final NotificationCenter notificationCenter;

    @Override
    public Result reconcile(Request request) {
        client.fetch(Reason.class, request.name()).ifPresent(reason -> {
            if (ExtensionUtil.isDeleted(reason)) {
                return;
            }
            if (ExtensionUtil.addFinalizers(reason.getMetadata(), Set.of(TRIGGERED_FINALIZER))) {
                // notifier
                onNewReasonReceived(reason);
                client.update(reason);
            }
        });
        return Result.doNotRetry();
    }

    private void onNewReasonReceived(Reason reason) {
        var name = reason.getMetadata().getName();
        log.info("Sending notification for reason: {}", name);
        notificationCenter.notify(reason).block(TIMEOUT);
        log.info("Notification sent for reason: {}", name);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Reason())
            .workerCount(10)
            .build();
    }
}
