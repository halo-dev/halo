package run.halo.app.extension.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.ReactiveSelectOperation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ReactiveExtensionStoreClientImplTest {

    @Mock
    ExtensionStoreRepository repository;

    @Mock
    R2dbcEntityOperations entityOperations;

    @InjectMocks
    ReactiveExtensionStoreClientImpl client;

    @Test
    void listByNamePrefix() {
        var expectedExtensions = List.of(
            new ExtensionStore("/registry/posts/hello-world", "this is post".getBytes(), 1L),
            new ExtensionStore("/registry/posts/hello-halo", "this is post".getBytes(), 1L)
        );

        var select = mock(ReactiveSelectOperation.ReactiveSelect.class);
        var selectWithQuery = mock(ReactiveSelectOperation.SelectWithQuery.class);
        var terminatingSelect = mock(ReactiveSelectOperation.TerminatingSelect.class);
        when(terminatingSelect.all()).thenReturn(Flux.fromIterable(expectedExtensions));
        when(selectWithQuery.matching(any())).thenReturn(terminatingSelect);
        when(select.withFetchSize(100)).thenReturn(selectWithQuery);
        when(entityOperations.select(ExtensionStore.class)).thenReturn(select);

        client.listByNamePrefix("/registry/posts").collectList()
            .as(StepVerifier::create)
            .expectNext(expectedExtensions)
            .verifyComplete();
    }

    @Test
    void fetchByName() {
        var expectedExtension =
            new ExtensionStore("/registry/posts/hello-world", "this is post".getBytes(), 1L);

        when(repository.findById("/registry/posts/hello-halo"))
            .thenReturn(Mono.just(expectedExtension));

        var gotExtension = client.fetchByName("/registry/posts/hello-halo").blockOptional();
        assertTrue(gotExtension.isPresent());
        assertEquals(expectedExtension, gotExtension.get());
    }

    @Test
    void create() {
        var expectedExtension =
            new ExtensionStore("/registry/posts/hello-halo", "hello halo".getBytes(), 2L);

        when(repository.save(any()))
            .thenReturn(Mono.just(expectedExtension));

        var createdExtension =
            client.create("/registry/posts/hello-halo", "hello halo".getBytes())
                .block();

        assertEquals(expectedExtension, createdExtension);
    }

    @Test
    void update() {
        var expectedExtension =
            new ExtensionStore("/registry/posts/hello-halo", "hello halo".getBytes(), 2L);

        when(repository.save(any())).thenReturn(Mono.just(expectedExtension));

        var updatedExtension =
            client.update("/registry/posts/hello-halo", 1L, "hello halo".getBytes())
                .block();

        assertEquals(expectedExtension, updatedExtension);
    }

    @Test
    void shouldDoNotThrowExceptionWhenDeletingNonExistExt() {
        when(repository.findById(anyString())).thenReturn(Mono.empty());

        client.delete("/registry/posts/hello-halo", 1L).block();
    }

    @Test
    void shouldDeleteSuccessfully() {
        var expectedExtension =
            new ExtensionStore("/registry/posts/hello-halo", "hello halo".getBytes(), 2L);

        when(repository.findById(anyString())).thenReturn(Mono.just(expectedExtension));
        when(repository.delete(any())).thenReturn(Mono.empty());

        var deletedExtension = client.delete("/registry/posts/hello-halo", 2L).block();

        assertEquals(expectedExtension, deletedExtension);
    }
}