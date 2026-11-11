package run.halo.app.notification;

import static run.halo.app.extension.index.query.Queries.equal;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.notification.Notification;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ReactiveExtensionPaginatedOperator;
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

    private final ReactiveExtensionPaginatedOperator paginatedOperator;

    @Override
    public Mono<ListResult<Notification>> listByUser(String username, UserNotificationQuery query) {
        return client.listBy(Notification.class, query.toListOptions(), query.toPageRequest());
    }

    @Override
    public Mono<Notification> getByName(String username, String name) {
        return client.get(Notification.class, name).doOnNext(notification -> {
            if (!isRecipient(notification, username)) {
                throw new AccessDeniedException("You have no permission to access this notification.");
            }
        });
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
    public Flux<String> markAllAsRead(String username) {
        var listOptions = ListOptions.builder()
                .andQuery(equal("spec.recipient", username))
                .build();
        return paginatedOperator
                .list(Notification.class, listOptions)
                .filter(notification -> notification.getSpec().isUnread())
                // Collect first so that pagination is not affected by the updates.
                .collectList()
                .flatMapMany(Flux::fromIterable)
                .flatMap(notification ->
                        markAsRead(username, notification.getMetadata().getName()))
                .map(notification -> notification.getMetadata().getName());
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
                        throw new AccessDeniedException("You have no permission to delete this notification.");
                    }
                })
                .flatMap(client::delete);
    }

    @Override
    public Mono<Void> deleteAllByUser(UserNotificationQuery query) {
        return paginatedOperator
                .deleteInitialBatch(Notification.class, query.toListOptions())
                .then();
    }

    static boolean isRecipient(Notification notification, String username) {
        Assert.notNull(notification, "Notification must not be null");
        Assert.notNull(username, "Username must not be null");
        return username.equals(notification.getSpec().getRecipient());
    }
}
