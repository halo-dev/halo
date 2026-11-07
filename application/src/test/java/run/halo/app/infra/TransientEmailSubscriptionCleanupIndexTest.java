package run.halo.app.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.Extension;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.index.IndexEngine;
import run.halo.app.extension.index.IndexSpecs;
import run.halo.app.extension.index.ValueIndexSpec;
import run.halo.app.notification.TransientEmailNotificationCoordinator;
import run.halo.app.notification.TransientEmailSubscriptionCleanupTask;
import run.halo.app.notification.UserIdentity;

class TransientEmailSubscriptionCleanupIndexTest {
    @ParameterizedTest
    @ValueSource(strings = {"email-verification", "login-email-code", "sudo-email-code", "reset-password-by-email"})
    void usesRegisteredIndexesToProtectOnlyMatchingPendingReasons(String reasonType) {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(ConversionService.class, DefaultConversionService::new);
            context.scan("run.halo.app.extension.index");
            context.refresh();
            var engine = context.getBean(IndexEngine.class);
            registerIndexes(engine);
            var pending = subscription("pending", reasonType);
            var unused = subscription("unused", reasonType);
            var longTerm = subscription("long-term", reasonType);
            longTerm.getSpec().getReason().setReasonType("new-device-login");
            var future = subscription("future", reasonType);
            future.getMetadata().setCreationTimestamp(Instant.now().plus(Duration.ofDays(1)));
            var store = new HashMap<String, Subscription>();
            for (var sub : List.of(pending, unused, longTerm, future)) {
                store.put(sub.getMetadata().getName(), sub);
            }
            engine.insert(store.values());

            var active = reason("active", pending);
            var otherType = reason("other-type", unused);
            otherType
                    .getSpec()
                    .setReasonType(reasonType.equals("login-email-code") ? "sudo-email-code" : "login-email-code");
            var otherApiVersion = reason("other-api", unused);
            otherApiVersion.getSpec().getSubject().setApiVersion("v1alpha1");
            var otherKind = reason("other-kind", unused);
            otherKind.getSpec().getSubject().setKind("Post");
            var otherName = reason("other-name", unused);
            otherName.getSpec().getSubject().setName("another-subscriber");
            var completed = reason("completed", unused);
            completed.getMetadata().setDeletionTimestamp(Instant.now());
            var reasons = new HashMap<String, Reason>();
            for (var reason : List.of(active, otherType, otherApiVersion, otherKind, otherName, completed)) {
                reasons.put(reason.getMetadata().getName(), reason);
            }
            for (int i = 0; i < 5000; i++) {
                var unrelated = reason("unrelated-" + i, subscription("another-" + i, reasonType));
                reasons.put(unrelated.getMetadata().getName(), unrelated);
            }
            engine.insert(reasons.values());
            var manager = spy(engine.getIndicesManager());
            var reasonIndices = spy(manager.get(Reason.class));
            doReturn(reasonIndices).when(manager).get(Reason.class);
            ReflectionTestUtils.setField(engine, "indicesManager", manager);

            var client = mock(ReactiveExtensionClient.class);
            when(client.listTopNames(eq(Subscription.class), any(), any(), anyInt()))
                    .thenAnswer(invocation -> Flux.defer(() -> Flux.fromIterable(engine.retrieveTopN(
                            Subscription.class,
                            invocation.getArgument(1),
                            invocation.getArgument(2),
                            invocation.getArgument(3)))));
            when(client.fetch(eq(Subscription.class), anyString()))
                    .thenAnswer(invocation -> Mono.fromSupplier(() -> store.get(invocation.getArgument(1))));
            when(client.listAll(eq(Reason.class), any(), any()))
                    .thenAnswer(invocation -> Flux.defer(() -> Flux.fromIterable(engine.retrieveAll(
                                    Reason.class, invocation.getArgument(1), invocation.getArgument(2))))
                            .map(reasons::get));
            var deleted = new ArrayList<String>();
            when(client.delete(any(Subscription.class)))
                    .thenAnswer(invocation -> Mono.fromSupplier(() -> {
                        Subscription sub = invocation.getArgument(0);
                        sub.getMetadata().setDeletionTimestamp(Instant.now());
                        engine.update(List.of(sub));
                        deleted.add(sub.getMetadata().getName());
                        return sub;
                    }));
            var task = new TransientEmailSubscriptionCleanupTask(client, new TransientEmailNotificationCoordinator());

            task.cleanUp();
            assertThat(deleted).containsExactly("unused");

            active.getMetadata().setDeletionTimestamp(Instant.now());
            engine.update(List.of(active));
            task.cleanUp();
            assertThat(deleted).containsExactly("unused", "pending");
            // Both matching and unrelated subjects must avoid global Reason index queries.
            verify(reasonIndices, never()).getIndex("metadata.deletionTimestamp");
            verify(reasonIndices, never()).getIndex("spec.reasonType");
        }
    }

    @Test
    void resumesAfterTimeBudgetPastProtectedRecordsAndWrapsAfterFinishing() {
        try (var context = indexContext()) {
            var engine = context.getBean(IndexEngine.class);
            registerIndexes(engine);
            var store = subscriptions(1005);
            engine.insert(store.values());
            var client = batchClient(engine, store);
            var protect = new AtomicBoolean(true);
            var reasons = new HashMap<String, Reason>();
            store.forEach((name, sub) -> {
                if (!name.equals("sub-1004")) {
                    reasons.put(name, reason(name, sub));
                }
            });
            engine.insert(reasons.values());
            when(client.listAll(eq(Reason.class), any(), any()))
                    .thenAnswer(invocation -> Flux.defer(() -> protect.get()
                                    ? Flux.fromIterable(engine.retrieveAll(
                                                    Reason.class, invocation.getArgument(1), invocation.getArgument(2)))
                                            .map(reasons::get)
                                    : Flux.empty())
                            .delaySubscription(Duration.ofSeconds(1)));
            var task = new TransientEmailSubscriptionCleanupTask(client, new TransientEmailNotificationCoordinator());

            StepVerifier.withVirtualTime(() -> runCleanup(task).delayElement(Duration.ofSeconds(2)))
                    .thenAwait(Duration.ofMinutes(10).plusSeconds(2))
                    .expectNext(0L)
                    .verifyComplete();
            var cursor = (String) ReflectionTestUtils.getField(task, "nameCursor");
            assertThat(cursor).isNotNull().isNotEqualTo("sub-1004");
            clearInvocations(client);
            // The second run must reach the tail without revisiting the protected prefix.
            StepVerifier.withVirtualTime(() -> runCleanup(task))
                    .thenAwait(Duration.ofMinutes(10))
                    .expectNext(1L)
                    .verifyComplete();
            verify(client, never()).fetch(Subscription.class, "sub-0000");
            verify(client, never()).fetch(Subscription.class, cursor);
            verify(client, atLeastOnce()).fetch(Subscription.class, "sub-1004");
            verify(client)
                    .delete(argThat(sub -> "sub-1004".equals(sub.getMetadata().getName())));
            assertThat(ReflectionTestUtils.getField(task, "nameCursor")).isNull();

            // Once the dependencies go away a wrapped scan revisits and deletes the old prefix.
            protect.set(false);
            StepVerifier.withVirtualTime(() -> runCleanup(task))
                    .thenAwait(Duration.ofMinutes(10))
                    .expectNextMatches(count -> count > 0)
                    .verifyComplete();
            verify(client)
                    .delete(argThat(sub -> "sub-0000".equals(sub.getMetadata().getName())));
        }
    }

    @Test
    void keysetBatchesDoNotSkipRecordsAsEarlierBatchesAreDeleted() {
        try (var context = indexContext()) {
            var engine = context.getBean(IndexEngine.class);
            registerIndexes(engine);
            var store = subscriptions(1005);
            engine.insert(store.values());
            var client = batchClient(engine, store);
            when(client.listAll(eq(Reason.class), any(), any())).thenReturn(Flux.empty());
            var task = new TransientEmailSubscriptionCleanupTask(client, new TransientEmailNotificationCoordinator());

            StepVerifier.withVirtualTime(() -> runCleanup(task))
                    .thenAwait(Duration.ofSeconds(20))
                    .expectNext(1005L)
                    .verifyComplete();

            verify(client, times(3)).listTopNames(eq(Subscription.class), any(), any(), eq(1000));
            verify(client, times(1005)).delete(any(Subscription.class));
            verify(client, never()).listAllNames(any(), any(), any());
            assertThat(ReflectionTestUtils.getField(task, "nameCursor")).isNull();
        }
    }

    private AnnotationConfigApplicationContext indexContext() {
        var context = new AnnotationConfigApplicationContext();
        context.registerBean(ConversionService.class, DefaultConversionService::new);
        context.scan("run.halo.app.extension.index");
        context.refresh();
        return context;
    }

    private Map<String, Subscription> subscriptions(int count) {
        var store = new HashMap<String, Subscription>();
        IntStream.range(0, count).forEach(i -> {
            var name = "sub-%04d".formatted(i);
            store.put(name, subscription(name, "email-verification"));
        });
        return store;
    }

    private ReactiveExtensionClient batchClient(IndexEngine engine, Map<String, Subscription> store) {
        var client = mock(ReactiveExtensionClient.class);
        when(client.listTopNames(eq(Subscription.class), any(), any(), anyInt()))
                .thenAnswer(invocation -> Flux.defer(() -> Flux.fromIterable(engine.retrieveTopN(
                        Subscription.class,
                        invocation.getArgument(1),
                        invocation.getArgument(2),
                        invocation.getArgument(3)))));
        when(client.fetch(eq(Subscription.class), anyString()))
                .thenAnswer(invocation -> Mono.just(store.get(invocation.getArgument(1))));
        when(client.delete(any(Subscription.class)))
                .thenAnswer(invocation -> Mono.fromSupplier(() -> {
                    Subscription sub = invocation.getArgument(0);
                    sub.getMetadata().setDeletionTimestamp(Instant.now());
                    engine.update(List.of(sub));
                    return sub;
                }));
        return client;
    }

    private Mono<Long> runCleanup(TransientEmailSubscriptionCleanupTask task) {
        return ReflectionTestUtils.invokeMethod(task, "cleanUpSubscriptions");
    }

    @SuppressWarnings("unchecked")
    private void registerIndexes(IndexEngine engine) {
        var schemeManager = mock(SchemeManager.class, CALLS_REAL_METHODS);
        doAnswer(invocation -> {
                    Class<Extension> type = invocation.getArgument(0);
                    Consumer<IndexSpecs<Extension>> consumer = invocation.getArgument(1);
                    if (!type.equals(Reason.class) && !type.equals(Subscription.class)) {
                        return null;
                    }
                    var specs = new ArrayList<ValueIndexSpec<Extension, ?>>();
                    var registry = mock(IndexSpecs.class, CALLS_REAL_METHODS);
                    doAnswer(add -> {
                                specs.add(add.getArgument(0));
                                return null;
                            })
                            .when(registry)
                            .add(any(ValueIndexSpec.class));
                    if (consumer != null) {
                        consumer.accept(registry);
                    }
                    engine.getIndicesManager().add(type, specs);
                    return null;
                })
                .when(schemeManager)
                .register(any(Class.class), any());
        new SchemeInitializer(schemeManager).start();
    }

    private Subscription subscription(String name, String reasonType) {
        var subscription = new Subscription();
        subscription.setMetadata(metadata(name));
        var spec = new Subscription.Spec();
        var subscriber = new Subscription.Subscriber();
        subscriber.setName(
                UserIdentity.anonymousWithEmail(name + "@example.com").name());
        spec.setSubscriber(subscriber);
        var reason = new Subscription.InterestReason();
        reason.setReasonType(reasonType);
        reason.setSubject(Subscription.ReasonSubject.builder()
                .apiVersion("User")
                .kind("User")
                .name(subscriber.getName())
                .build());
        spec.setReason(reason);
        subscription.setSpec(spec);
        return subscription;
    }

    private Reason reason(String name, Subscription subscription) {
        var reason = new Reason();
        reason.setMetadata(metadata(name));
        var subject = subscription.getSpec().getReason().getSubject();
        reason.setSpec(new Reason.Spec()
                .setReasonType(subscription.getSpec().getReason().getReasonType())
                .setSubject(Reason.Subject.builder()
                        .apiVersion(subject.getApiVersion())
                        .kind(subject.getKind())
                        .name(subject.getName())
                        .build()));
        return reason;
    }

    private Metadata metadata(String name) {
        var metadata = new Metadata();
        metadata.setName(name);
        metadata.setVersion(1L);
        metadata.setCreationTimestamp(Instant.now().minus(Duration.ofDays(400)));
        return metadata;
    }
}
