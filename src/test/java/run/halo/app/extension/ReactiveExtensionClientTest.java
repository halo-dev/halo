package run.halo.app.extension;

import static java.util.Collections.emptyList;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.extension.GroupVersionKind.fromAPIVersionAndKind;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.exception.SchemeNotFoundException;
import run.halo.app.extension.store.ExtensionStore;
import run.halo.app.extension.store.ReactiveExtensionStoreClient;

@ExtendWith(MockitoExtension.class)
class ReactiveExtensionClientTest {

    static final Scheme fakeScheme = Scheme.buildFromType(FakeExtension.class);

    @Mock
    ReactiveExtensionStoreClient storeClient;

    @Mock
    ExtensionConverter converter;

    @Mock
    SchemeManager schemeManager;

    @Spy
    ObjectMapper objectMapper = JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .build();

    @InjectMocks
    ReactiveExtensionClientImpl client;

    @BeforeEach
    void setUp() {
        lenient().when(schemeManager.get(eq(FakeExtension.class)))
            .thenReturn(fakeScheme);
        lenient().when(schemeManager.get(eq(fakeScheme.groupVersionKind()))).thenReturn(fakeScheme);
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

        when(converter.convertTo(any())).thenThrow(SchemeNotFoundException.class);
        StepVerifier.create(client.create(createFakeExtension("fake", null)))
            .verifyError(SchemeNotFoundException.class);

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
        when(storeClient.listByNamePrefix(anyString())).thenReturn(Flux.empty());
        var fakes = client.list(FakeExtension.class, null, null);
        StepVerifier.create(fakes)
            .verifyComplete();
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
            Flux.fromIterable(
                List.of(createExtensionStore("fake-01"), createExtensionStore("fake-02"))));

        // without filter and sorter
        var fakes = client.list(FakeExtension.class, null, null);
        StepVerifier.create(fakes)
            .expectNext(fake1)
            .expectNext(fake2)
            .verifyComplete();

        // with filter
        fakes = client.list(FakeExtension.class, fake -> {
            String name = fake.getMetadata().getName();
            return "fake-01".equals(name);
        }, null);
        StepVerifier.create(fakes)
            .expectNext(fake1)
            .verifyComplete();

        // with sorter
        fakes = client.list(FakeExtension.class, null,
            reverseOrder(comparing(fake -> fake.getMetadata().getName())));
        StepVerifier.create(fakes)
            .expectNext(fake2)
            .expectNext(fake1)
            .verifyComplete();
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

        when(storeClient.listByNamePrefix(anyString())).thenReturn(Flux.fromIterable(
            List.of(createExtensionStore("fake-01"), createExtensionStore("fake-02"),
                createExtensionStore("fake-03"))));

        // without filter and sorter.
        var fakes = client.list(FakeExtension.class, null, null, 1, 10);
        StepVerifier.create(fakes)
            .expectNext(new ListResult<>(1, 10, 3, List.of(fake1, fake2, fake3)))
            .verifyComplete();

        // out of page range
        fakes = client.list(FakeExtension.class, null, null, 100, 10);
        StepVerifier.create(fakes)
            .expectNext(new ListResult<>(100, 10, 3, emptyList()))
            .verifyComplete();

        // with filter only
        fakes =
            client.list(FakeExtension.class, fake -> "fake-03".equals(fake.getMetadata().getName()),
                null, 1, 10);
        StepVerifier.create(fakes)
            .expectNext(new ListResult<>(1, 10, 1, List.of(fake3)))
            .verifyComplete();

        // with sorter only
        fakes = client.list(FakeExtension.class, null,
            reverseOrder(comparing(fake -> fake.getMetadata().getName())), 1, 10);
        StepVerifier.create(fakes)
            .expectNext(new ListResult<>(1, 10, 3, List.of(fake3, fake2, fake1)))
            .verifyComplete();

        // without page
        fakes = client.list(FakeExtension.class, null, null, 0, 0);
        StepVerifier.create(fakes)
            .expectNext(new ListResult<>(0, 0, 3, List.of(fake1, fake2, fake3)))
            .verifyComplete();
    }

    @Test
    void shouldFetchNothing() {
        when(storeClient.fetchByName(any())).thenReturn(Mono.empty());

        var fake = client.fetch(FakeExtension.class, "fake");

        StepVerifier.create(fake)
            .verifyComplete();

        verify(converter, times(0)).convertFrom(any(), any());
        verify(storeClient, times(1)).fetchByName(any());
    }

    @Test
    void shouldNotFetchUnstructured() {
        when(schemeManager.get(isA(GroupVersionKind.class)))
            .thenReturn(fakeScheme);
        when(storeClient.fetchByName(any())).thenReturn(Mono.empty());
        var unstructuredFake = client.fetch(fakeScheme.groupVersionKind(), "fake");
        StepVerifier.create(unstructuredFake)
            .verifyComplete();

        verify(converter, times(0)).convertFrom(any(), any());
        verify(schemeManager, times(1)).get(isA(GroupVersionKind.class));
        verify(storeClient, times(1)).fetchByName(any());
    }

    @Test
    void shouldFetchAnExtension() {
        var storeName = "/registry/fake.halo.run/fakes/fake";
        when(storeClient.fetchByName(storeName)).thenReturn(
            Mono.just(createExtensionStore(storeName)));

        when(
            converter.convertFrom(FakeExtension.class, createExtensionStore(storeName))).thenReturn(
            createFakeExtension("fake", 1L));

        var fake = client.fetch(FakeExtension.class, "fake");
        StepVerifier.create(fake)
            .expectNext(createFakeExtension("fake", 1L))
            .verifyComplete();

        verify(storeClient, times(1)).fetchByName(eq(storeName));
        verify(converter, times(1)).convertFrom(eq(FakeExtension.class),
            eq(createExtensionStore(storeName)));
    }

    @Test
    void shouldFetchUnstructuredExtension() throws JsonProcessingException {
        var storeName = "/registry/fake.halo.run/fakes/fake";
        when(storeClient.fetchByName(storeName)).thenReturn(
            Mono.just(createExtensionStore(storeName)));
        when(schemeManager.get(isA(GroupVersionKind.class)))
            .thenReturn(fakeScheme);
        when(converter.convertFrom(Unstructured.class, createExtensionStore(storeName)))
            .thenReturn(createUnstructured());

        var fake = client.fetch(fakeScheme.groupVersionKind(), "fake");
        StepVerifier.create(fake)
            .expectNext(createUnstructured())
            .verifyComplete();

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
            Mono.just(createExtensionStore("/registry/fake.halo.run/fakes/fake")));
        when(converter.convertFrom(same(FakeExtension.class), any())).thenReturn(fake);

        StepVerifier.create(client.create(fake))
            .expectNext(fake)
            .verifyComplete();

        verify(converter, times(1)).convertTo(eq(fake));
        verify(storeClient, times(1)).create(eq("/registry/fake.halo.run/fakes/fake"), any());
        assertNotNull(fake.getMetadata().getCreationTimestamp());
    }

    @Test
    void shouldCreateWithGenerateNameSuccessfully() {
        var fake = createFakeExtension("fake", null);
        fake.getMetadata().setName("");
        fake.getMetadata().setGenerateName("fake-");
        when(converter.convertTo(any())).thenReturn(
            createExtensionStore("/registry/fake.halo.run/fakes/fake"));
        when(storeClient.create(any(), any())).thenReturn(
            Mono.just(createExtensionStore("/registry/fake.halo.run/fakes/fake")));
        when(converter.convertFrom(same(FakeExtension.class), any())).thenReturn(fake);

        StepVerifier.create(client.create(fake))
            .expectNext(fake)
            .verifyComplete();

        verify(converter, times(1)).convertTo(argThat(ext -> {
            var name = ext.getMetadata().getName();
            return name.startsWith(ext.getMetadata().getGenerateName());
        }));
        verify(storeClient, times(1)).create(eq("/registry/fake.halo.run/fakes/fake"), any());
        assertNotNull(fake.getMetadata().getCreationTimestamp());
    }

    @Test
    void shouldThrowExceptionIfCreatingWithoutGenerateName() {
        var fake = createFakeExtension("fake", null);
        fake.getMetadata().setName("");
        fake.getMetadata().setGenerateName("");

        StepVerifier.create(client.create(fake))
            .verifyError(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionIfPrimaryKeyDuplicated() {
        var fake = createFakeExtension("fake", null);
        fake.getMetadata().setName("");
        fake.getMetadata().setGenerateName("fake-");
        when(converter.convertTo(any())).thenReturn(
            createExtensionStore("/registry/fake.halo.run/fakes/fake"));
        when(storeClient.create(any(), any())).thenThrow(DataIntegrityViolationException.class);

        StepVerifier.create(client.create(fake))
            .expectErrorMatches(Exceptions::isRetryExhausted)
            .verify();
    }

    @Test
    void shouldCreateUsingUnstructuredSuccessfully() throws JsonProcessingException {
        var fake = createUnstructured();

        when(converter.convertTo(any())).thenReturn(
            createExtensionStore("/registry/fake.halo.run/fakes/fake"));
        when(storeClient.create(any(), any())).thenReturn(
            Mono.just(createExtensionStore("/registry/fake.halo.run/fakes/fake")));
        when(converter.convertFrom(same(Unstructured.class), any())).thenReturn(fake);

        StepVerifier.create(client.create(fake))
            .expectNext(fake)
            .verifyComplete();

        verify(converter, times(1)).convertTo(eq(fake));
        verify(storeClient, times(1)).create(eq("/registry/fake.halo.run/fakes/fake"), any());
        assertNotNull(fake.getMetadata().getCreationTimestamp());
    }

    @Test
    void shouldUpdateSuccessfully() {
        var fake = createFakeExtension("fake", 2L);
        fake.getMetadata().setLabels(Map.of("new", "true"));
        var storeName = "/registry/fake.halo.run/fakes/fake";
        when(converter.convertTo(any())).thenReturn(
            createExtensionStore(storeName, 2L));
        when(storeClient.update(any(), any(), any())).thenReturn(
            Mono.just(createExtensionStore(storeName, 2L)));
        when(storeClient.fetchByName(storeName)).thenReturn(
            Mono.just(createExtensionStore(storeName, 1L)));

        var oldFake = createFakeExtension("fake", 2L);
        oldFake.getMetadata().setLabels(Map.of("old", "true"));

        var updatedFake = createFakeExtension("fake", 3L);
        updatedFake.getMetadata().setLabels(Map.of("updated", "true"));
        when(converter.convertFrom(same(FakeExtension.class), any()))
            .thenReturn(oldFake)
            .thenReturn(updatedFake);

        StepVerifier.create(client.update(fake))
            .expectNext(updatedFake)
            .verifyComplete();

        verify(storeClient).fetchByName(storeName);
        verify(converter).convertTo(isA(JsonExtension.class));
        verify(converter, times(2)).convertFrom(same(FakeExtension.class), any());
        verify(storeClient)
            .update(eq("/registry/fake.halo.run/fakes/fake"), eq(2L), any());
    }

    @Test
    void shouldNotUpdateIfExtensionNotChange() {
        var fake = createFakeExtension("fake", 2L);
        var storeName = "/registry/fake.halo.run/fakes/fake";
        when(storeClient.fetchByName(storeName)).thenReturn(
            Mono.just(createExtensionStore(storeName, 1L)));

        var oldFake = createFakeExtension("fake", 2L);

        when(converter.convertFrom(same(FakeExtension.class), any())).thenReturn(oldFake);

        StepVerifier.create(client.update(fake))
            .expectNext(fake)
            .verifyComplete();

        verify(storeClient).fetchByName(storeName);
        verify(converter).convertFrom(same(FakeExtension.class), any());
        verify(converter, never()).convertTo(any());
        verify(storeClient, never()).update(any(), any(), any());
    }

    @Test
    void shouldUpdateUnstructuredSuccessfully() throws JsonProcessingException {
        var fake = createUnstructured();
        var name = "/registry/fake.halo.run/fakes/fake";
        when(converter.convertTo(any()))
            .thenReturn(createExtensionStore(name, 12345L));
        when(storeClient.update(any(), any(), any()))
            .thenReturn(Mono.just(createExtensionStore(name, 12345L)));
        when(storeClient.fetchByName(name))
            .thenReturn(Mono.just(createExtensionStore(name, 12346L)));

        var oldFake = createUnstructured();
        oldFake.getMetadata().setLabels(Map.of("old", "true"));

        var updatedFake = createUnstructured();
        updatedFake.getMetadata().setLabels(Map.of("updated", "true"));
        when(converter.convertFrom(same(Unstructured.class), any()))
            .thenReturn(oldFake)
            .thenReturn(updatedFake);

        StepVerifier.create(client.update(fake))
            .expectNext(updatedFake)
            .verifyComplete();

        verify(storeClient).fetchByName(name);
        verify(converter).convertTo(isA(JsonExtension.class));
        verify(converter, times(2)).convertFrom(same(Unstructured.class), any());
        verify(storeClient)
            .update(eq("/registry/fake.halo.run/fakes/fake"), eq(12345L), any());
    }

    @Test
    void shouldDeleteSuccessfully() {
        var fake = createFakeExtension("fake", 2L);
        when(converter.convertTo(any())).thenReturn(
            createExtensionStore("/registry/fake.halo.run/fakes/fake"));
        when(storeClient.update(any(), any(), any())).thenReturn(
            Mono.just(createExtensionStore("/registry/fake.halo.run/fakes/fake")));
        when(converter.convertFrom(same(FakeExtension.class), any())).thenReturn(fake);

        StepVerifier.create(client.delete(fake))
            .expectNext(fake)
            .verifyComplete();

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
        void shouldNotWatchOnUpdateIfExtensionNotChange() {
            shouldNotUpdateIfExtensionNotChange();

            verify(watcher, never()).onUpdate(any(), any());
        }

        @Test
        void shouldWatchOnDeleteSuccessfully() {
            doNothing().when(watcher).onDelete(any());
            shouldDeleteSuccessfully();

            verify(watcher, times(1)).onDelete(any());
        }
    }

}