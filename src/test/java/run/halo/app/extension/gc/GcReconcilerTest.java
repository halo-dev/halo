package run.halo.app.extension.gc;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionConverter;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Unstructured;
import run.halo.app.extension.store.ExtensionStore;
import run.halo.app.extension.store.ExtensionStoreClient;

@ExtendWith(MockitoExtension.class)
class GcReconcilerTest {

    @Mock
    ExtensionClient client;

    @Mock
    ExtensionStoreClient storeClient;

    @Mock
    ExtensionConverter converter;

    @InjectMocks
    GcReconciler reconciler;

    @Test
    void shouldDoNothingIfExtensionNotFound() {
        var fake = createExtension();
        when(client.fetch(fake.groupVersionKind(), fake.getMetadata().getName()))
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
        when(client.fetch(fake.groupVersionKind(), fake.getMetadata().getName()))
            .thenReturn(Optional.of(convertTo(fake)));

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
        when(client.fetch(fake.groupVersionKind(), fake.getMetadata().getName()))
            .thenReturn(Optional.of(convertTo(fake)));

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
        when(client.fetch(fake.groupVersionKind(), fake.getMetadata().getName()))
            .thenReturn(Optional.of(convertTo(fake)));

        ExtensionStore store = new ExtensionStore();
        store.setName("fake-store-name");
        store.setVersion(1L);

        when(converter.convertTo(any())).thenReturn(store);

        var result = reconciler.reconcile(createGcRequest());
        assertNull(result);
        verify(converter).convertTo(any());
        verify(storeClient).delete("fake-store-name", 1L);
    }

    GcRequest createGcRequest() {
        var fake = createExtension();
        return new GcRequest(fake.groupVersionKind(), fake.getMetadata().getName());
    }

    Unstructured convertTo(FakeExtension fake) {
        return Unstructured.OBJECT_MAPPER.convertValue(fake, Unstructured.class);
    }

    FakeExtension createExtension() {
        var fake = new FakeExtension();
        var metadata = new Metadata();
        metadata.setName("fake");
        fake.setMetadata(metadata);
        return fake;
    }
}
