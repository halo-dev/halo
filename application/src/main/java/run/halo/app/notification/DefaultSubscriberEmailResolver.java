package run.halo.app.notification;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * <p>Default implementation of {@link SubscriberEmailResolver}.</p>
 * <p>If the subscriber is an anonymous subscriber, the email will be extracted from the
 * subscriber name.</p>
 * <p>An anonymous subscriber's name is in the format of {@code anonymous#email}.</p>
 *
 * @author guqing
 * @since 2.10.0
 */
@Component
@RequiredArgsConstructor
public class DefaultSubscriberEmailResolver implements SubscriberEmailResolver {
    private final ReactiveExtensionClient client;

    @Override
    public Mono<String> resolve(Subscription.Subscriber subscriber) {
        var identity = UserIdentity.of(subscriber.getName());
        if (identity.isAnonymous()) {
            return Mono.fromSupplier(() -> getEmail(subscriber));
        }
        return client.fetch(User.class, subscriber.getName())
            .filter(user -> user.getSpec().isEmailVerified())
            .mapNotNull(user -> user.getSpec().getEmail());
    }

    @Override
    public Subscription.Subscriber ofEmail(String email) {
        if (StringUtils.isBlank(email)) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        var subscriber = new Subscription.Subscriber();
        subscriber.setName(UserIdentity.anonymousWithEmail(email).name());
        return subscriber;
    }

    @NonNull
    String getEmail(Subscription.Subscriber subscriber) {
        var identity = UserIdentity.of(subscriber.getName());
        if (!identity.isAnonymous()) {
            throw new IllegalStateException("The subscriber is not an anonymous subscriber");
        }
        return identity.getEmail()
            .filter(StringUtils::isNotBlank)
            .orElseThrow(() -> new IllegalStateException("The subscriber does not have an email"));
    }
}
