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
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.extension.GroupVersionKind.fromAPIVersionAndKind;
import static run.halo.app.extension.Scheme.buildFromType;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.exception.SchemeNotFoundException;
import run.halo.app.extension.store.ExtensionStore;
import run.halo.app.extension.store.ExtensionStoreClient;

@ExtendWith(MockitoExtension.class)
class DefaultExtensionClientTest {

    static final Scheme fakeScheme = Scheme.buildFromType(FakeExtension.class);

    @Mock
    ExtensionStoreClient storeClient;

    @Mock
    ExtensionConverter converter;

    @Mock
    SchemeManager schemeManager;

    @Mock
    ReactiveExtensionClient reactiveClient;

    @InjectMocks
    DefaultExtensionClient client;

    @BeforeEach
    void setUp() {
        lenient().when(schemeManager.get(eq(FakeExtension.class)))
            .thenReturn(buildFromType(FakeExtension.class));
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
        when(schemeManager.get(isA(GroupVersionKind.class)))
            .thenThrow(SchemeNotFoundException.class);

        assertThrows(SchemeNotFoundException.class,
            () -> client.list(UnRegisteredExtension.class, null, null));
        assertThrows(SchemeNotFoundException.class,
            () -> client.list(UnRegisteredExtension.class, null, null, 0, 10));
        assertThrows(SchemeNotFoundException.class,
            () -> client.fetch(UnRegisteredExtension.class, "fake"));
        assertThrows(SchemeNotFoundException.class, () ->
            client.fetch(fromAPIVersionAndKind("fake.halo.run/v1alpha1", "UnRegistered"), "fake"));
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
        var fakes = client.list(FakeExtension.class, null, null, 1, 10);
        assertEquals(new ListResult<>(1, 10, 3, List.of(fake1, fake2, fake3)), fakes);

        // out of page range
        fakes = client.list(FakeExtension.class, null, null, 100, 10);
        assertEquals(new ListResult<>(100, 10, 3, emptyList()), fakes);

        // with filter only
        fakes =
            client.list(FakeExtension.class, fake -> "fake-03".equals(fake.getMetadata().getName()),
                null, 1, 10);
        assertEquals(new ListResult<>(1, 10, 1, List.of(fake3)), fakes);

        // with sorter only
        fakes = client.list(FakeExtension.class, null,
            reverseOrder(comparing(fake -> fake.getMetadata().getName())), 1, 10);
        assertEquals(new ListResult<>(1, 10, 3, List.of(fake3, fake2, fake1)), fakes);

        // without page
        fakes = client.list(FakeExtension.class, null, null, 0, 0);
        assertEquals(new ListResult<>(0, 0, 3, List.of(fake1, fake2, fake3)), fakes);
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
    void shouldNotFetchUnstructured() {
        when(schemeManager.get(isA(GroupVersionKind.class)))
            .thenReturn(fakeScheme);
        when(storeClient.fetchByName(any())).thenReturn(Optional.empty());
        var unstructuredFake = client.fetch(fakeScheme.groupVersionKind(), "fake");

        assertEquals(Optional.empty(), unstructuredFake);
        verify(converter, times(0)).convertFrom(any(), any());
        verify(schemeManager, times(1)).get(isA(GroupVersionKind.class));
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
    void shouldFetchUnstructuredExtension() throws JsonProcessingException {
        var storeName = "/registry/fake.halo.run/fakes/fake";
        when(storeClient.fetchByName(storeName)).thenReturn(
            Optional.of(createExtensionStore(storeName)));
        when(schemeManager.get(isA(GroupVersionKind.class)))
            .thenReturn(fakeScheme);
        when(converter.convertFrom(Unstructured.class, createExtensionStore(storeName)))
            .thenReturn(createUnstructured());

        var fake = client.fetch(fakeScheme.groupVersionKind(), "fake");

        assertEquals(Optional.of(createUnstructured()), fake);
        verify(storeClient, times(1)).fetchByName(eq(storeName));
        verify(schemeManager, times(1)).get(isA(GroupVersionKind.class));
        verify(converter, times(1)).convertFrom(eq(Unstructured.class),
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
        when(storeClient.update(any(), any(), any())).thenReturn(
            createExtensionStore("/registry/fake.halo.run/fakes/fake"));

        client.delete(fake);

        verify(converter, times(1)).convertTo(any());
        verify(storeClient, times(1)).update(any(), any(), any());
        verify(storeClient, never()).delete(any(), any());
    }

    @Nested
    @DisplayName("Extension watcher test")
    class WatcherTest {

        @Mock
        Watcher watcher;

        @BeforeEach
        void setUp() {
            client.watch(watcher);
            verify(reactiveClient).watch(watcher);
        }

        @Test
        void shouldWatchOnAddSuccessfully() {
            doNothing().when(watcher).onAdd(any());
            shouldCreateSuccessfully();

            verify(watcher, times(1)).onAdd(any());
        }

        @Test
        void shouldWatchOnUpdateSuccessfully() {
            doNothing().when(watcher).onUpdate(any(), any());
            shouldUpdateSuccessfully();

            verify(watcher, times(1)).onUpdate(any(), any());
        }

        @Test
        void shouldWatchOnDeleteSuccessfully() {
            doNothing().when(watcher).onDelete(any());
            shouldDeleteSuccessfully();

            verify(watcher, times(1)).onDelete(any());
        }
    }

}