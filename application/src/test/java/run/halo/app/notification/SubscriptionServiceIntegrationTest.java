package run.halo.app.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static run.halo.app.extension.index.query.QueryFactory.isNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionStoreUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.index.IndexerFactory;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.extension.store.ReactiveExtensionStoreClient;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Integration tests for {@link SubscriptionService}.
 *
 * @author guqing
 * @since 2.15.0
 */
@DirtiesContext
@SpringBootTest
class SubscriptionServiceIntegrationTest {

    @Autowired
    private SchemeManager schemeManager;

    @SpyBean
    private ReactiveExtensionClient client;

    @Autowired
    private ReactiveExtensionStoreClient storeClient;

    @Autowired
    private IndexerFactory indexerFactory;

    Mono<Extension> deleteImmediately(Extension extension) {
        var name = extension.getMetadata().getName();
        var scheme = schemeManager.get(extension.getClass());
        // un-index
        var indexer = indexerFactory.getIndexer(extension.groupVersionKind());
        indexer.unIndexRecord(extension.getMetadata().getName());

        // delete from db
        var storeName = ExtensionStoreUtil.buildStoreName(scheme, name);
        return storeClient.delete(storeName, extension.getMetadata().getVersion())
            .thenReturn(extension);
    }

    @Nested
    class RemoveInitialBatchTest {
        static int size = 310;
        private final List<Subscription> storedSubscriptions = subscriptionsForStore();

        @Autowired
        private SubscriptionService subscriptionService;

        @BeforeEach
        void setUp() {
            Flux.fromIterable(storedSubscriptions)
                .flatMap(comment -> client.create(comment))
                .as(StepVerifier::create)
                .expectNextCount(storedSubscriptions.size())
                .verifyComplete();
        }

        @AfterEach
        void tearDown() {
            Flux.fromIterable(storedSubscriptions)
                .flatMap(SubscriptionServiceIntegrationTest.this::deleteImmediately)
                .as(StepVerifier::create)
                .expectNextCount(storedSubscriptions.size())
                .verifyComplete();
        }

        private List<Subscription> subscriptionsForStore() {
            List<Subscription> subscriptions = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                var subscription = createSubscription();
                subscription.getMetadata().setName("subscription-" + i);
                subscriptions.add(subscription);
            }
            return subscriptions;
        }

        @Test
        void removeTest() {
            var subscriber = new Subscription.Subscriber();
            subscriber.setName("admin");
            var interestReason = new Subscription.InterestReason();
            interestReason.setReasonType("new-comment-on-post");
            var subject = new Subscription.ReasonSubject();
            subject.setApiVersion("content.halo.run/v1alpha1");
            subject.setKind("Post");
            interestReason.setSubject(subject);

            subscriptionService.remove(subscriber, interestReason).block();

            verify(client, atLeast(size)).delete(any(Subscription.class));
            assertCleanedUp();
        }

        @Test
        void removeBySubscriberTest() {
            var subscriber = new Subscription.Subscriber();
            subscriber.setName("admin");

            subscriptionService.remove(subscriber).block();
            verify(client, atLeast(size)).delete(any(Subscription.class));
            assertCleanedUp();
        }

        private void assertCleanedUp() {
            var listOptions = new ListOptions();
            listOptions.setFieldSelector(FieldSelector.of(isNull("metadata.deletionTimestamp")));
            client.listBy(Subscription.class, listOptions, PageRequestImpl.ofSize(1))
                .as(StepVerifier::create)
                .consumeNextWith(result -> {
                    assertThat(result.getTotal()).isEqualTo(0);
                    assertThat(result.getItems()).isEmpty();
                })
                .verifyComplete();
        }
    }

    Subscription createSubscription() {
        return JsonUtils.jsonToObject("""
            {
                "spec": {
                    "subscriber": {
                        "name": "admin"
                    },
                    "unsubscribeToken": "423530c9-bec7-446e-b73b-dd98ac00ba2b",
                    "reason": {
                        "reasonType": "new-comment-on-post",
                        "subject": {
                            "name": "5152aea5-c2e8-4717-8bba-2263d46e19d5",
                            "apiVersion": "content.halo.run/v1alpha1",
                            "kind": "Post"
                        }
                    },
                    "disabled": false
                },
                "apiVersion": "notification.halo.run/v1alpha1",
                "kind": "Subscription",
                "metadata": {
                    "generateName": "subscription-"
                }
            }
            """, Subscription.class);
    }
}