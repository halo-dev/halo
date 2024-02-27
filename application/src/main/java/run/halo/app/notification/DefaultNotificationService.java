package run.halo.app.notification;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.notification.Notification;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.AccessDeniedException;

/**
 * A default implementation of {@link UserNotificationService}.
 *
 * @author guqing
 * @since 2.10.0
 */
@Component
@RequiredArgsConstructor
public class DefaultNotificationService implements UserNotificationService {

    private final ReactiveExtensionClient client;

    @Override
    public Mono<ListResult<Notification>> listByUser(String username, UserNotificationQuery query) {
        return client.listBy(Notification.class, query.toListOptions(), query.toPageRequest());
    }

    @Override
    public Mono<Notification> markAsRead(String username, String name) {
        return client.fetch(Notification.class, name)
            .filter(notification -> isRecipient(notification, username))
            .flatMap(notification -> {
                notification.getSpec().setUnread(false);
                notification.getSpec().setLastReadAt(Instant.now());
                return client.update(notification);
            });
    }

    @Override
    public Flux<String> markSpecifiedAsRead(String username, List<String> names) {
        return Flux.fromIterable(names)
            .flatMap(name -> markAsRead(username, name))
            .map(notification -> notification.getMetadata().getName());
    }

    @Override
    public Mono<Notification> deleteByName(String username, String name) {
        return client.get(Notification.class, name)
            .doOnNext(notification -> {
                var recipient = notification.getSpec().getRecipient();
                if (!username.equals(recipient)) {
                    throw new AccessDeniedException(
                        "You have no permission to delete this notification.");
                }
            })
            .flatMap(client::delete);
    }

    static boolean isRecipient(Notification notification, String username) {
        Assert.notNull(notification, "Notification must not be null");
        Assert.notNull(username, "Username must not be null");
        return username.equals(notification.getSpec().getRecipient());
    }
}
