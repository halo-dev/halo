package run.halo.app.extension.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExtensionStoreClientJPAImplTest {

    @Mock
    ExtensionStoreRepository repository;

    @InjectMocks
    ExtensionStoreClientJPAImpl client;

    @Test
    void listByNamePrefix() {
        var expectedExtensions = List.of(
            new ExtensionStore("/registry/posts/hello-world", "this is post".getBytes(), 1L),
            new ExtensionStore("/registry/posts/hello-halo", "this is post".getBytes(), 1L)
        );

        when(repository.findAllByNameStartingWith("/registry/posts"))
            .thenReturn(expectedExtensions);

        var gotExtensions = client.listByNamePrefix("/registry/posts");
        assertEquals(expectedExtensions, gotExtensions);
    }

    @Test
    void fetchByName() {
        var expectedExtension =
            new ExtensionStore("/registry/posts/hello-world", "this is post".getBytes(), 1L);

        when(repository.findById("/registry/posts/hello-halo")).thenReturn(
            Optional.of(expectedExtension));

        var gotExtension = client.fetchByName("/registry/posts/hello-halo");
        assertTrue(gotExtension.isPresent());
        assertEquals(expectedExtension, gotExtension.get());
    }

    @Test
    void create() {
        var expectedExtension =
            new ExtensionStore("/registry/posts/hello-halo", "hello halo".getBytes(), 2L);

        when(repository.save(any())).thenReturn(expectedExtension);

        var createdExtension =
            client.create("/registry/posts/hello-halo", "hello halo".getBytes());

        assertEquals(expectedExtension, createdExtension);
    }

    @Test
    void update() {
        var expectedExtension =
            new ExtensionStore("/registry/posts/hello-halo", "hello halo".getBytes(), 2L);

        when(repository.save(any())).thenReturn(expectedExtension);

        var updatedExtension =
            client.update("/registry/posts/hello-halo", 1L, "hello halo".getBytes());

        assertEquals(expectedExtension, updatedExtension);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenDeletingNonExistExt() {

        when(repository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> client.delete("/registry/posts/hello-halo", 1L));
    }

    @Test
    void shouldDeleteSuccessfully() {
        var expectedExtension =
            new ExtensionStore("/registry/posts/hello-halo", "hello halo".getBytes(), 2L);

        when(repository.findById(any())).thenReturn(Optional.of(expectedExtension));
        doNothing().when(repository).delete(any());

        var deletedExtension = client.delete("/registry/posts/hello-halo", 2L);

        assertEquals(expectedExtension, deletedExtension);
    }
}