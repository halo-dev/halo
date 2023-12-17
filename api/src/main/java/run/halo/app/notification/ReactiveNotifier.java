package run.halo.app.notification;

import org.pf4j.ExtensionPoint;
import reactor.core.publisher.Mono;

/**
 * Notifier to notify user.
 *
 * @author guqing
 * @since 2.10.0
 */
public interface ReactiveNotifier extends ExtensionPoint {

    /**
     * Notify user.
     *
     * @param context notification context must not be null
     */
    Mono<Void> notify(NotificationContext context);
}
