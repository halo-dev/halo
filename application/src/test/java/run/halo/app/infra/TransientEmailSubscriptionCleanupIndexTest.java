package run.halo.app.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
            engine.insert(List.of(active, otherType, otherApiVersion, otherKind, otherName, completed));

            var client = mock(ReactiveExtensionClient.class);
            when(client.listAllNames(eq(Subscription.class), any(), any()))
                    .thenAnswer(invocation -> Flux.defer(() -> Flux.fromIterable(engine.retrieveAll(
                            Subscription.class, invocation.getArgument(1), invocation.getArgument(2)))));
            when(client.fetch(eq(Subscription.class), anyString()))
                    .thenAnswer(invocation -> Mono.fromSupplier(() -> store.get(invocation.getArgument(1))));
            when(client.listTopNames(eq(Reason.class), any(), any(), anyInt()))
                    .thenAnswer(invocation -> Flux.defer(() -> Flux.fromIterable(engine.retrieveTopN(
                            Reason.class,
                            invocation.getArgument(1),
                            invocation.getArgument(2),
                            invocation.getArgument(3)))));
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
        }
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
