package run.halo.app.extension.gc;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionConverter;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.index.IndexEngine;
import run.halo.app.extension.store.Extensions;
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
        when(client.fetch(FakeExtension.class, fake.getMetadata().getName()))
            .thenReturn(Optional.empty());

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
        when(client.fetch(FakeExtension.class, fake.getMetadata().getName()))
            .thenReturn(Optional.of(fake));

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
        when(client.fetch(FakeExtension.class, fake.getMetadata().getName()))
            .thenReturn(Optional.of(fake));

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
        when(client.fetch(FakeExtension.class, fake.getMetadata().getName()))
            .thenReturn(Optional.of(fake));

        Extensions store = new Extensions();
        store.setName("fake-store-name");
        store.setVersion(1L);

        when(converter.convertTo(any())).thenReturn(store);
        doNothing().when(indexEngine).delete(any());
        var tx = mock(ReactiveTransaction.class);
        when(txManager.getReactiveTransaction(any())).thenReturn(Mono.just(tx));
        when(txManager.commit(tx)).thenReturn(Mono.empty());
        when(storeClient.delete("fake-store-name", 1L)).thenReturn(Mono.just(store));

        var result = reconciler.reconcile(createGcRequest());
        assertNull(result);
        verify(converter).convertTo(any());
        verify(storeClient).delete("fake-store-name", 1L);
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
