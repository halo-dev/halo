package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import run.halo.app.extension.event.SchemeAddedEvent;
import run.halo.app.extension.event.SchemeRemovedEvent;
import run.halo.app.extension.exception.SchemeNotFoundException;
import run.halo.app.extension.index.IndexEngine;
import run.halo.app.extension.index.IndicesManager;

@ExtendWith(MockitoExtension.class)
class DefaultSchemeManagerTest {

    @Mock
    IndicesManager indicesManager;

    @Mock
    IndexEngine indexEngine;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Mock
    run.halo.app.extension.index.IndexSnapshotManager snapshots;

    @Mock
    org.springframework.beans.factory.ObjectProvider<run.halo.app.extension.index.IndexSnapshotManager>
            snapshotProvider;

    @InjectMocks
    DefaultSchemeManager schemeManager;

    @BeforeEach
    void setUp() {
        lenient().when(indexEngine.getIndicesManager()).thenReturn(indicesManager);
        lenient().when(snapshotProvider.getObject()).thenReturn(snapshots);
        org.mockito.Mockito.lenient()
                .doAnswer(call -> {
                    call.getArgument(1, Runnable.class).run();
                    return null;
                })
                .when(snapshots)
                .unregister(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenNoGvkAnnotation() {
        class WithoutGvkExtension extends AbstractExtension {}

        assertThrows(IllegalArgumentException.class, () -> schemeManager.register(WithoutGvkExtension.class));
    }

    @Test
    void shouldGetNothingWhenUnregistered() {
        final var gvk = new GroupVersionKind("fake.halo.run", "v1alpha1", "Fake");
        var scheme = schemeManager.fetch(gvk);
        assertFalse(scheme.isPresent());

        assertThrows(SchemeNotFoundException.class, () -> schemeManager.get(gvk));
        assertThrows(SchemeNotFoundException.class, () -> schemeManager.get(FakeExtension.class));
        assertThrows(SchemeNotFoundException.class, () -> schemeManager.get(new FakeExtension()));
    }

    @Test
    void shouldGetSchemeWhenRegistered() {
        schemeManager.register(FakeExtension.class);
        final var gvk = new GroupVersionKind("fake.halo.run", "v1alpha1", "Fake");
        var scheme = schemeManager.fetch(gvk);
        assertTrue(scheme.isPresent());

        assertEquals(gvk, schemeManager.get(gvk).groupVersionKind());
        assertEquals(gvk, schemeManager.get(FakeExtension.class).groupVersionKind());
        assertEquals(gvk, schemeManager.get(new FakeExtension()).groupVersionKind());
    }

    @Test
    void shouldUnregisterSuccessfully() {
        schemeManager.register(FakeExtension.class);
        Scheme scheme = schemeManager.get(FakeExtension.class);
        assertNotNull(scheme);

        schemeManager.unregister(scheme);
        assertThrows(SchemeNotFoundException.class, () -> schemeManager.get(FakeExtension.class));
    }

    @Test
    void shouldTriggerOnChangeOnlyOnceWhenRegisterTwice() {
        schemeManager.register(FakeExtension.class);
        schemeManager.register(FakeExtension.class);

        verify(eventPublisher).publishEvent(isA(SchemeAddedEvent.class));
        verify(indicesManager).add(same(FakeExtension.class), any());
    }

    @Test
    void shouldTriggerOnChangeOnlyOnceWhenUnregisterTwice() {
        schemeManager.register(FakeExtension.class);
        var scheme = schemeManager.get(FakeExtension.class);
        schemeManager.unregister(scheme);
        schemeManager.unregister(scheme);

        verify(eventPublisher).publishEvent(isA(SchemeAddedEvent.class));
        verify(eventPublisher).publishEvent(isA(SchemeRemovedEvent.class));
        verify(indicesManager).add(same(FakeExtension.class), any());
    }

    @Test
    void getSizeOfSchemes() {
        assertEquals(0, schemeManager.size());
        schemeManager.register(FakeExtension.class);
        assertEquals(1, schemeManager.size());
        schemeManager.unregister(schemeManager.get(FakeExtension.class));
        assertEquals(0, schemeManager.size());
    }

    @Test
    void shouldReturnCopyOnWriteList() {
        schemeManager.register(FakeExtension.class);
        var schemes = schemeManager.schemes();
        schemes.forEach(scheme -> {
            // make sure concurrent modification won't happen
            schemeManager.register(FooExtension.class);
        });
    }

    @Test
    void interruptedRegistrationMustRollBackAndAllowRetry() {
        var engine = run.halo.app.extension.index.IndexEngineTestSupport.newIndexEngine();
        var attempts = new java.util.concurrent.atomic.AtomicInteger();
        var removals = new java.util.concurrent.atomic.AtomicInteger();
        var resource = new ConfigMap();
        var metadata = new Metadata();
        metadata.setName("existing");
        metadata.setCreationTimestamp(java.time.Instant.EPOCH);
        resource.setMetadata(metadata);
        var failure = new IllegalStateException("cancelled during index construction");
        ApplicationEventPublisher publisher = event -> {
            if (event instanceof SchemeAddedEvent) {
                engine.insert(java.util.List.of(resource));
                if (attempts.getAndIncrement() == 0) {
                    Thread.currentThread().interrupt();
                    throw failure;
                }
            } else if (event instanceof SchemeRemovedEvent) {
                removals.incrementAndGet();
            }
        };
        var manager = new DefaultSchemeManager(engine, publisher, snapshotProvider);
        try {
            assertSame(failure, assertThrows(IllegalStateException.class, () -> manager.register(ConfigMap.class)));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
        assertTrue(manager.schemes().isEmpty());
        assertThrows(
                IllegalArgumentException.class, () -> engine.getIndicesManager().get(ConfigMap.class));
        assertEquals(1, removals.get());
        manager.register(ConfigMap.class);
        assertEquals(2, attempts.get());
        assertEquals(1, manager.size());
        org.assertj.core.api.Assertions.assertThat(engine.retrieveAll(ConfigMap.class, null, null))
                .containsExactly("existing");
    }

    @Test
    void registrationMustPreserveOriginalFailureIfRemovalListenerFails() {
        var failure = new IllegalStateException("registration failed");
        var cleanupFailure = new IllegalArgumentException("listener cleanup failed");
        org.mockito.Mockito.doThrow(failure).when(eventPublisher).publishEvent(isA(SchemeAddedEvent.class));
        org.mockito.Mockito.doThrow(cleanupFailure).when(eventPublisher).publishEvent(isA(SchemeRemovedEvent.class));
        assertSame(
                failure, assertThrows(IllegalStateException.class, () -> schemeManager.register(FakeExtension.class)));
        assertArrayEquals(new Throwable[] {cleanupFailure}, failure.getSuppressed());
        assertTrue(schemeManager.schemes().isEmpty());
        verify(indicesManager).remove(FakeExtension.class);
    }

    @GVK(group = "fake.halo.run", version = "v1alpha1", kind = "Foo", plural = "foos", singular = "foo")
    static class FooExtension extends AbstractExtension {}
}
