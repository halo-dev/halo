package run.halo.app.notification.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ExternalUrlSupplier;

/**
 * Tests for {@link SubscriptionRouter}.
 *
 * @author guqing
 * @since 2.9.0
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionRouterTest {

    @Mock
    private ReactiveExtensionClient client;

    @Mock
    private ExternalUrlSupplier externalUrlSupplier;

    @InjectMocks
    SubscriptionRouter subscriptionRouter;

    @Test
    void getUnsubscribeUrlTest() {
        when(externalUrlSupplier.get()).thenReturn(URI.create("https://halo.run"));
        var subscription = new Subscription();
        subscription.setMetadata(new Metadata());
        subscription.getMetadata().setName("fake-subscription");
        subscription.setSpec(new Subscription.Spec());
        subscription.getSpec().setUnsubscribeToken("fake-unsubscribe-token");

        var url = subscriptionRouter.getUnsubscribeUrl(subscription);
        assertThat(url).isEqualTo("https://halo.run/apis/api.notification.halo.run/v1alpha1"
            + "/subscriptions/fake-subscription/unsubscribe"
            + "?token=fake-unsubscribe-token");
    }

}