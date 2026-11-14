package run.halo.app.extension.gc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.reactive.AbstractReactiveTransactionManager;
import org.springframework.transaction.reactive.GenericReactiveTransaction;
import org.springframework.transaction.reactive.TransactionSynchronizationManager;
import reactor.core.publisher.Mono;
import run.halo.app.extension.*;
import run.halo.app.extension.index.IndexEngine;
import run.halo.app.extension.store.ExtensionStore;
import run.halo.app.extension.store.ReactiveExtensionStoreClient;

@ExtendWith(MockitoExtension.class)
class GcReconcilerTest {

    @Mock
    ExtensionClient client;

    @Mock
    ReactiveExtensionStoreClient storeClient;

    @Mock
    ExtensionConverter converter;

    @Mock
    SchemeManager schemeManager;

    @Mock
    IndexEngine indexEngine;

    @Mock
    ReactiveTransactionManager txManager;

    @Spy
    IndexOperationRegistrar indexOpRegistrar = new IndexOperationRegistrar();

    @InjectMocks
    GcReconciler reconciler;

    @BeforeEach
    void setUp() {
        var scheme = Scheme.buildFromType(FakeExtension.class);
        when(schemeManager.get(scheme.groupVersionKind())).thenReturn(scheme);
    }

    @Test
    void shouldDoNothingIfExtensionNotFound() {
        var fake = createExtension();
        when(client.fetch(FakeExtension.class, fake.getMetadata().getName())).thenReturn(Optional.empty());

        var result = reconciler.reconcile(createGcRequest());
        assertNull(result);
        verify(converter, never()).convertTo(any());
        verify(storeClient, never()).delete(any(), any());
    }

    @Test
    void shouldDoNothingIfFinalizersPresent() {
        var fake = createExtension();
        fake.getMetadata().setFinalizers(Set.of("fake-finalizer"));
        fake.getMetadata().setDeletionTimestamp(null);
        when(client.fetch(FakeExtension.class, fake.getMetadata().getName())).thenReturn(Optional.of(fake));

        var result = reconciler.reconcile(createGcRequest());
        assertNull(result);
        verify(converter, never()).convertTo(any());
        verify(storeClient, never()).delete(any(), any());
    }

    @Test
    void shouldDoNothingIfDeletionTimestampIsNull() {
        var fake = createExtension();
        fake.getMetadata().setDeletionTimestamp(null);
        fake.getMetadata().setFinalizers(null);
        when(client.fetch(FakeExtension.class, fake.getMetadata().getName())).thenReturn(Optional.of(fake));

        var result = reconciler.reconcile(createGcRequest());
        assertNull(result);
        verify(converter, never()).convertTo(any());
        verify(storeClient, never()).delete(any(), any());
    }

    @Test
    void shouldDeleteCorrectly() {
        var fake = createExtension();
        fake.getMetadata().setDeletionTimestamp(Instant.now());
        fake.getMetadata().setFinalizers(null);
        when(client.fetch(FakeExtension.class, fake.getMetadata().getName())).thenReturn(Optional.of(fake));

        ExtensionStore store = new ExtensionStore();
        store.setName("fake-store-name");
        store.setVersion(1L);

        when(converter.convertTo(any())).thenReturn(store);
        doNothing().when(indexEngine).delete(any());
        when(storeClient.delete("fake-store-name", 1L)).thenReturn(Mono.just(store));

        var reconciler = new GcReconciler(
                client,
                storeClient,
                converter,
                schemeManager,
                indexEngine,
                new FakeReactiveTransactionManager(),
                indexOpRegistrar);

        var result = reconciler.reconcile(createGcRequest());
        assertNull(result);
        verify(converter).convertTo(any());
        verify(storeClient).delete("fake-store-name", 1L);
    }

    @Test
    void indexDeleteShouldBeDeferredToAfterCommit() {
        var fake = createExtension();
        fake.getMetadata().setDeletionTimestamp(Instant.now());
        fake.getMetadata().setFinalizers(null);
        when(client.fetch(FakeExtension.class, fake.getMetadata().getName())).thenReturn(Optional.of(fake));

        ExtensionStore store = new ExtensionStore();
        store.setName("fake-store-name");
        store.setVersion(1L);
        when(converter.convertTo(any())).thenReturn(store);
        when(storeClient.delete("fake-store-name", 1L)).thenReturn(Mono.just(store));

        var indexDeleted = new AtomicBoolean(false);
        doAnswer(invocation -> {
                    indexDeleted.set(true);
                    return null;
                })
                .when(indexEngine)
                .delete(any());

        var transactionManager = new FakeReactiveTransactionManager();
        var indexDeletedBeforeCommit = new AtomicBoolean(false);
        transactionManager.commitHook = () -> indexDeletedBeforeCommit.set(indexDeleted.get());
        var reconciler = new GcReconciler(
                client, storeClient, converter, schemeManager, indexEngine, transactionManager, indexOpRegistrar);

        var result = reconciler.reconcile(createGcRequest());
        assertNull(result);
        assertFalse(indexDeletedBeforeCommit.get(), "Index must not be updated before the commit");
        verify(indexEngine, times(1)).delete(any());
    }

    @Test
    void indexDeleteShouldBeSkippedWhenTransactionRollsBack() {
        var fake = createExtension();
        fake.getMetadata().setDeletionTimestamp(Instant.now());
        fake.getMetadata().setFinalizers(null);
        when(client.fetch(FakeExtension.class, fake.getMetadata().getName())).thenReturn(Optional.of(fake));

        ExtensionStore store = new ExtensionStore();
        store.setName("fake-store-name");
        store.setVersion(1L);
        when(converter.convertTo(any())).thenReturn(store);
        when(storeClient.delete("fake-store-name", 1L)).thenReturn(Mono.just(store));

        var transactionManager = new FakeReactiveTransactionManager();
        transactionManager.failOnCommit = true;
        var reconciler = new GcReconciler(
                client, storeClient, converter, schemeManager, indexEngine, transactionManager, indexOpRegistrar);

        assertThrows(Exception.class, () -> reconciler.reconcile(createGcRequest()));
        verify(indexEngine, never()).delete(any());
    }

    /**
     * Minimal in-memory {@link ReactiveTransactionManager} that drives the real
     * {@link AbstractReactiveTransactionManager} commit/rollback lifecycle, including transaction synchronizations.
     * Duplicated from {@code ReactiveExtensionClientTest} because that fixture is package-private.
     */
    static class FakeReactiveTransactionManager extends AbstractReactiveTransactionManager {

        Runnable commitHook = () -> {};

        boolean failOnCommit;

        @Override
        protected Object doGetTransaction(TransactionSynchronizationManager synchronizationManager) {
            return new Object();
        }

        @Override
        protected Mono<Void> doBegin(
                TransactionSynchronizationManager synchronizationManager,
                Object transaction,
                TransactionDefinition definition) {
            return Mono.empty();
        }

        @Override
        protected Mono<Void> doCommit(
                TransactionSynchronizationManager synchronizationManager, GenericReactiveTransaction transaction) {
            if (failOnCommit) {
                return Mono.error(new IllegalStateException("Commit failed on purpose"));
            }
            return Mono.fromRunnable(commitHook);
        }

        @Override
        protected Mono<Void> doRollback(
                TransactionSynchronizationManager synchronizationManager, GenericReactiveTransaction transaction) {
            return Mono.empty();
        }
    }

    GcRequest createGcRequest() {
        var fake = createExtension();
        return new GcRequest(fake.groupVersionKind(), fake.getMetadata().getName());
    }

    FakeExtension createExtension() {
        var fake = new FakeExtension();
        var metadata = new Metadata();
        metadata.setName("fake");
        fake.setMetadata(metadata);
        return fake;
    }
}
