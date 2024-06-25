package run.halo.app.notification;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.DefaultController;
import run.halo.app.extension.controller.DefaultQueue;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequestQueue;
import run.halo.app.plugin.extensionpoint.ExtensionDefinition;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

/**
 * A default {@link NotificationSender} implementation.
 *
 * @author guqing
 * @since 2.10.0
 */
@Slf4j
@Component
public class DefaultNotificationSender
    implements NotificationSender, Reconciler<DefaultNotificationSender.QueueItem>,
    SmartLifecycle {
    private final ReactiveExtensionClient client;
    private final ExtensionGetter extensionGetter;

    private final RequestQueue<QueueItem> requestQueue;

    private final Controller controller;

    private boolean running = false;

    /**
     * Constructs a new notification sender with the given {@link ReactiveExtensionClient} and
     * {@link ExtensionGetter}.
     */
    public DefaultNotificationSender(ReactiveExtensionClient client,
        ExtensionGetter extensionGetter) {
        this.client = client;
        this.extensionGetter = extensionGetter;
        requestQueue = new DefaultQueue<>(Instant::now);
        controller = this.setupWith(null);
    }

    @Override
    public Mono<Void> sendNotification(String notifierExtensionName, NotificationContext context) {
        return selectNotifier(notifierExtensionName)
            .doOnNext(notifier -> {
                var item = new QueueItem(UUID.randomUUID().toString(),
                    () -> notifier.notify(context).block(), 0);
                requestQueue.addImmediately(item);
            })
            .then();
    }

    Mono<ReactiveNotifier> selectNotifier(String notifierExtensionName) {
        return client.fetch(ExtensionDefinition.class, notifierExtensionName)
            .flatMap(extDefinition -> extensionGetter.getEnabledExtensions(
                    ReactiveNotifier.class)
                .filter(notifier -> notifier.getClass().getName()
                    .equals(extDefinition.getSpec().getClassName())
                )
                .next()
            );
    }

    @Override
    public Result reconcile(QueueItem request) {
        if (request.getTimes() > 3) {
            log.error("Failed to send notification after retrying 3 times, discard it.");
            return Result.doNotRetry();
        }
        log.debug("Executing send notification task, [{}] remaining to-do tasks",
            requestQueue.size());
        request.setTimes(request.getTimes() + 1);
        request.getTask().execute();
        return Result.doNotRetry();
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return new DefaultController<>(
            this.getClass().getName(),
            this,
            requestQueue,
            null,
            Duration.ofMillis(100),
            Duration.ofSeconds(1000),
            5
        );
    }

    @Override
    public void start() {
        controller.start();
        running = true;
    }

    @Override
    public void stop() {
        running = false;
        controller.dispose();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * <p>Queue item for {@link #requestQueue}.</p>
     * <p>It holds a {@link SendNotificationTask} and a {@link #times} field.</p>
     * <p>{@link SendNotificationTask} used to send email when consuming.</p>
     * <p>{@link #times} will be used to record the number of
     * times the task has been executed, if retry three times on failure, it will be discarded.</p>
     * <p>It also holds a {@link #id} field, which is used to identify the item. queue item with
     * the same id is considered to be the same item to ensure that controller can
     * discard the existing item in the queue when item re-queued on failure.</p>
     */
    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class QueueItem {

        @EqualsAndHashCode.Include
        private final String id;

        private final SendNotificationTask task;

        @Setter
        private int times;
    }

    @FunctionalInterface
    interface SendNotificationTask {
        void execute();
    }
}

