package run.halo.app.extension;

import static java.util.Collections.emptyList;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import run.halo.app.extension.exception.SchemeNotFoundException;
import run.halo.app.extension.store.ExtensionStore;
import run.halo.app.extension.store.ExtensionStoreClient;

@ExtendWith(MockitoExtension.class)
class DefaultExtensionClientTest {

    @Mock
    ExtensionStoreClient storeClient;

    @Mock
    ExtensionConverter converter;

    @Mock
    SchemeManager schemeManager;

    @InjectMocks
    DefaultExtensionClient client;

    @BeforeEach
    void setUp() {
        lenient().when(schemeManager.get(eq(FakeExtension.class)))
            .thenReturn(Scheme.buildFromType(FakeExtension.class));
    }

    FakeExtension createFakeExtension(String name, Long version) {
        var fake = new FakeExtension();
        var metadata = new Metadata();
        metadata.setName(name);
        metadata.setVersion(version);

        fake.setMetadata(metadata);
        fake.setApiVersion("fake.halo.run/v1alpha1");
        fake.setKind("Fake");

        return fake;
    }

    ExtensionStore createExtensionStore(String name) {
        return createExtensionStore(name, null);
    }

    ExtensionStore createExtensionStore(String name, Long version) {
        var extensionStore = new ExtensionStore();
        extensionStore.setName(name);
        extensionStore.setVersion(version);
        return extensionStore;
    }

    Unstructured createUnstructured() throws JsonProcessingException {
        String extensionJson = """
            {
                "apiVersion": "fake.halo.run/v1alpha1",
                "kind": "Fake",
                "metadata": {
                    "labels": {
                        "category": "fake",
                        "default": "true"
                    },
                    "name": "fake",
                    "creationTimestamp": "2011-12-03T10:15:30Z",
                    "version": 12345
                }
            }
            """;
        return Unstructured.OBJECT_MAPPER.readValue(extensionJson, Unstructured.class);
    }

    @Test
    void shouldThrowSchemeNotFoundExceptionWhenSchemeNotRegistered() {
        class UnRegisteredExtension extends AbstractExtension {
        }

        when(schemeManager.get(eq(UnRegisteredExtension.class)))
            .thenThrow(SchemeNotFoundException.class);

        assertThrows(SchemeNotFoundException.class,
            () -> client.list(UnRegisteredExtension.class, null, null));
        assertThrows(SchemeNotFoundException.class,
            () -> client.page(UnRegisteredExtension.class, null, null, 0, 10));
        assertThrows(SchemeNotFoundException.class,
            () -> client.fetch(UnRegisteredExtension.class, "fake"));
        assertThrows(SchemeNotFoundException.class, () -> {
            when(converter.convertTo(any())).thenThrow(SchemeNotFoundException.class);
            client.create(createFakeExtension("fake", null));
        });
        assertThrows(SchemeNotFoundException.class, () -> {
            when(converter.convertTo(any())).thenThrow(SchemeNotFoundException.class);
            client.update(createFakeExtension("fake", 1L));
        });
        assertThrows(SchemeNotFoundException.class, () -> {
            when(converter.convertTo(any())).thenThrow(SchemeNotFoundException.class);
            client.delete(createFakeExtension("fake", 1L));
        });
    }

    @Test
    void shouldReturnEmptyExtensions() {
        when(storeClient.listByNamePrefix(anyString())).thenReturn(emptyList());
        var fakes = client.list(FakeExtension.class, null, null);
        assertEquals(emptyList(), fakes);
    }

    @Test
    void shouldReturnExtensionsWithFilterAndSorter() {
        var fake1 = createFakeExtension("fake-01", 1L);
        var fake2 = createFakeExtension("fake-02", 1L);

        when(
            converter.convertFrom(FakeExtension.class, createExtensionStore("fake-01"))).thenReturn(
            fake1);
        when(
            converter.convertFrom(FakeExtension.class, createExtensionStore("fake-02"))).thenReturn(
            fake2);
        when(storeClient.listByNamePrefix(anyString())).thenReturn(
            List.of(createExtensionStore("fake-01"), createExtensionStore("fake-02")));

        // without filter and sorter
        var fakes = client.list(FakeExtension.class, null, null);
        assertEquals(List.of(fake1, fake2), fakes);

        // with filter
        fakes = client.list(FakeExtension.class, fake -> {
            String name = fake.getMetadata().getName();
            return "fake-01".equals(name);
        }, null);
        assertEquals(List.of(fake1), fakes);

        // with sorter
        fakes = client.list(FakeExtension.class, null,
            reverseOrder(comparing(fake -> fake.getMetadata().getName())));
        assertEquals(List.of(fake2, fake1), fakes);
    }

    @Test
    void shouldQueryPageableAndCorrectly() {
        var fake1 = createFakeExtension("fake-01", 1L);
        var fake2 = createFakeExtension("fake-02", 1L);
        var fake3 = createFakeExtension("fake-03", 1L);

        when(
            converter.convertFrom(FakeExtension.class, createExtensionStore("fake-01"))).thenReturn(
            fake1);
        when(
            converter.convertFrom(FakeExtension.class, createExtensionStore("fake-02"))).thenReturn(
            fake2);
        when(
            converter.convertFrom(FakeExtension.class, createExtensionStore("fake-03"))).thenReturn(
            fake3);

        when(storeClient.listByNamePrefix(anyString())).thenReturn(
            List.of(createExtensionStore("fake-01"), createExtensionStore("fake-02"),
                createExtensionStore("fake-03")));

        // without filter and sorter.
        var fakes = client.page(FakeExtension.class, null, null, 0, 10);
        assertEquals(new PageImpl<>(List.of(fake1, fake2, fake3), PageRequest.of(0, 10), 3), fakes);

        // out of page range
        fakes = client.page(FakeExtension.class, null, null, 100, 10);
        assertEquals(new PageImpl<>(emptyList(), PageRequest.of(100, 10), 3), fakes);

        // with filter only
        fakes =
            client.page(FakeExtension.class, fake -> "fake-03".equals(fake.getMetadata().getName()),
                null, 0, 10);
        assertEquals(new PageImpl<>(List.of(fake3), PageRequest.of(0, 10), 1), fakes);

        // with sorter only
        fakes = client.page(FakeExtension.class, null,
            reverseOrder(comparing(fake -> fake.getMetadata().getName())), 0, 10);
        assertEquals(new PageImpl<>(List.of(fake3, fake2, fake1), PageRequest.of(0, 10), 3), fakes);
    }

    @Test
    void shouldFetchNothing() {
        when(storeClient.fetchByName(any())).thenReturn(Optional.empty());

        Optional<FakeExtension> fake = client.fetch(FakeExtension.class, "fake");

        assertEquals(Optional.empty(), fake);
        verify(converter, times(0)).convertFrom(any(), any());
        verify(storeClient, times(1)).fetchByName(any());
    }

    @Test
    void shouldFetchAnExtension() {
        var storeName = "/registry/fake.halo.run/fakes/fake";
        when(storeClient.fetchByName(storeName)).thenReturn(
            Optional.of(createExtensionStore(storeName)));

        when(
            converter.convertFrom(FakeExtension.class, createExtensionStore(storeName))).thenReturn(
            createFakeExtension("fake", 1L));

        Optional<FakeExtension> fake = client.fetch(FakeExtension.class, "fake");
        assertEquals(Optional.of(createFakeExtension("fake", 1L)), fake);

        verify(storeClient, times(1)).fetchByName(eq(storeName));
        verify(converter, times(1)).convertFrom(eq(FakeExtension.class),
            eq(createExtensionStore(storeName)));
    }

    @Test
    void shouldCreateSuccessfully() {
        var fake = createFakeExtension("fake", null);
        when(converter.convertTo(any())).thenReturn(
            createExtensionStore("/registry/fake.halo.run/fakes/fake"));
        when(storeClient.create(any(), any())).thenReturn(
            createExtensionStore("/registry/fake.halo.run/fakes/fake"));

        client.create(fake);

        verify(converter, times(1)).convertTo(eq(fake));
        verify(storeClient, times(1)).create(eq("/registry/fake.halo.run/fakes/fake"), any());
        assertNotNull(fake.getMetadata().getCreationTimestamp());
    }

    @Test
    void shouldCreateUsingUnstructuredSuccessfully() throws JsonProcessingException {
        var fake = createUnstructured();

        when(converter.convertTo(any())).thenReturn(
            createExtensionStore("/registry/fake.halo.run/fakes/fake"));
        when(storeClient.create(any(), any())).thenReturn(
            createExtensionStore("/registry/fake.halo.run/fakes/fake"));

        client.create(fake);

        verify(converter, times(1)).convertTo(eq(fake));
        verify(storeClient, times(1)).create(eq("/registry/fake.halo.run/fakes/fake"), any());
        assertNotNull(fake.getMetadata().getCreationTimestamp());
    }

    @Test
    void shouldUpdateSuccessfully() {
        var fake = createFakeExtension("fake", 2L);
        when(converter.convertTo(any())).thenReturn(
            createExtensionStore("/registry/fake.halo.run/fakes/fake", 2L));
        when(storeClient.update(any(), any(), any())).thenReturn(
            createExtensionStore("/registry/fake.halo.run/fakes/fake", 2L));

        client.update(fake);

        verify(converter, times(1)).convertTo(eq(fake));
        verify(storeClient, times(1))
            .update(eq("/registry/fake.halo.run/fakes/fake"), eq(2L), any());
    }

    @Test
    void shouldUpdateUnstructuredSuccessfully() throws JsonProcessingException {
        var fake = createUnstructured();
        when(converter.convertTo(any())).thenReturn(
            createExtensionStore("/registry/fake.halo.run/fakes/fake", 12345L));
        when(storeClient.update(any(), any(), any())).thenReturn(
            createExtensionStore("/registry/fake.halo.run/fakes/fake", 12345L));

        client.update(fake);

        verify(converter, times(1)).convertTo(eq(fake));
        verify(storeClient, times(1))
            .update(eq("/registry/fake.halo.run/fakes/fake"), eq(12345L), any());
    }

    @Test
    void shouldDeleteSuccessfully() {
        var fake = createFakeExtension("fake", 2L);
        when(converter.convertTo(any())).thenReturn(
            createExtensionStore("/registry/fake.halo.run/fakes/fake"));
        when(storeClient.delete(any(), any())).thenReturn(
            createExtensionStore("/registry/fake.halo.run/fakes/fake"));

        client.delete(fake);

        verify(converter, times(1)).convertTo(any());
        verify(storeClient, times(1)).delete(any(), any());
    }

}