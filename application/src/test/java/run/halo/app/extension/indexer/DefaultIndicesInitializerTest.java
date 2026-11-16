package run.halo.app.extension.indexer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.*;
import run.halo.app.extension.index.*;
import run.halo.app.extension.store.*;

class DefaultIndicesInitializerTest {
    IndexEngine engine;
    ExtensionStoreClient store;
    ExtensionConverter converter;
    IndexSnapshotManager snapshots;
    DefaultIndicesInitializer initializer;
    Scheme scheme = Scheme.buildFromType(FakePost.class);

    @BeforeEach
    void setUp() {
        engine = IndexEngineTestSupport.newIndexEngine();
        store = mock(ExtensionStoreClient.class);
        converter = mock(ExtensionConverter.class);
        snapshots = mock(IndexSnapshotManager.class);
        initializer = new DefaultIndicesInitializer(engine, store, converter, snapshots);
        register(1);
    }

    void register(int version) {
        engine.getIndicesManager()
                .add(
                        FakePost.class,
                        List.of(IndexSpecs.<FakePost, String>single("slug", String.class)
                                .indexFunc(p -> p.slug)
                                .version(version)
                                .build()));
    }

    void stubDatabase() {
        var row = new ExtensionStore("/registry/fakeposts/current", new byte[0], 1L);
        when(store.listBy(anyString(), any(), anyInt())).thenReturn(List.of(row), List.of());
        when(converter.convertFrom(FakePost.class, row)).thenReturn(new FakePost("current", "database", Set.of()));
    }

    @Test
    void fullBuildShouldUseDatabaseCursorAndConvertRows() {
        stubDatabase();
        doAnswer(call -> {
                    call.getArgument(1, Runnable.class).run();
                    return null;
                })
                .when(snapshots)
                .initialize(eq(scheme), any());
        initializer.initialize(scheme);
        assertThat(engine.retrieveAll(FakePost.class, null, null)).containsExactly("current");
        verify(store).listBy("/registry/fakeposts", null, 100);
        verify(store).listBy("/registry/fakeposts", "/registry/fakeposts/current", 100);
    }

    @Test
    void restoredIndicesMustNotQueryOrConvertDatabaseRows() {
        engine.insert(List.of(new FakePost("restored", "snapshot", Set.of())));
        initializer.initialize(scheme);
        assertThat(engine.retrieveAll(FakePost.class, null, null)).containsExactly("restored");
        verify(snapshots).initialize(eq(scheme), any());
        verifyNoInteractions(store, converter);
    }

    @Test
    void failedInitializationMustDiscardPartialIndicesBeforeReleasingQueries() {
        var failure = new IllegalStateException("interrupted initialization");
        doAnswer(call -> {
                    engine.insert(List.of(new FakePost("partial", "partial", Set.of())));
                    throw failure;
                })
                .when(snapshots)
                .initialize(eq(scheme), any());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> initializer.initialize(scheme))
                .isSameAs(failure);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> engine.retrieveAll(FakePost.class, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No indices found");
        register(1);
        doNothing().when(snapshots).initialize(eq(scheme), any());
        initializer.initialize(scheme);
        assertThat(engine.retrieveAll(FakePost.class, null, null)).isEmpty();
    }

    @GVK(group = "", version = "v1alpha1", kind = "FakePost", plural = "fakeposts", singular = "fakepost")
    static class FakePost implements Extension {

        private final Metadata metadata = new Metadata();

        String slug;

        Set<String> tags;

        FakePost(String name, String slug, Set<String> tags) {
            metadata.setName(name);
            metadata.setCreationTimestamp(Instant.now());
            this.slug = slug;
            this.tags = tags;
        }

        @Override
        public Metadata getMetadata() {
            return metadata;
        }

        @Override
        public void setMetadata(MetadataOperator metadata) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setApiVersion(String apiVersion) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setKind(String kind) {
            throw new UnsupportedOperationException();
        }
    }
}
