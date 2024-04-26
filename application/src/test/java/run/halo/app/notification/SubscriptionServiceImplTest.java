package run.halo.app.notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Tests for {@link SubscriptionServiceImpl}.
 *
 * @author guqing
 * @since 2.15.0
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @Test
    void remove() {
        var i = new AtomicLong(1L);
        when(client.delete(any(Subscription.class))).thenAnswer(invocation -> {
            var subscription = (Subscription) invocation.getArgument(0);
            if (i.get() != subscription.getMetadata().getVersion()) {
                return Mono.error(new OptimisticLockingFailureException("fake-exception"));
            }
            return Mono.just(subscription);
        });

        var subscription = new Subscription();
        subscription.setMetadata(new Metadata());
        subscription.getMetadata().setName("fake-subscription");
        subscription.getMetadata().setVersion(0L);

        when(client.fetch(eq(Subscription.class), eq("fake-subscription")))
            .thenAnswer(invocation -> {
                if (i.incrementAndGet() > 3) {
                    subscription.getMetadata().setVersion(i.get());
                } else {
                    subscription.getMetadata().setVersion(i.get() - 1);
                }
                return Mono.just(subscription);
            });

        subscriptionService.remove(subscription)
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();

        // give version=0, but the real version is 1
        // give version=1, but the real version is 2
        // give version=2, but the real version is 3
        // give version=3, but the real version is 3 (delete success)
        verify(client, times(3)).fetch(eq(Subscription.class), eq("fake-subscription"));
    }
}