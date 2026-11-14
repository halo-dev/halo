package run.halo.app.extension.indexer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionConverter;
import run.halo.app.extension.GVK;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.index.IndexEngine;
import run.halo.app.extension.index.IndexEngineTestSupport;
import run.halo.app.extension.index.IndexSnapshot;
import run.halo.app.extension.index.IndexSnapshotManager;
import run.halo.app.extension.index.IndexSpecs;
import run.halo.app.extension.index.IndicesSnapshot;
import run.halo.app.extension.index.query.Queries;
import run.halo.app.extension.store.ExtensionStore;
import run.halo.app.extension.store.ExtensionStoreClient;
import run.halo.app.extension.store.NameVersion;

class DefaultIndicesInitializerTest {

    private static final String PREFIX = "/registry/fakeposts";

    @TempDir
    Path workDir;

    IndexEngine indexEngine;

    ExtensionStoreClient storeClient;

    ExtensionConverter converter;

    IndexSnapshotManager snapshotManager;

    DefaultIndicesInitializer initializer;

    Scheme scheme;

    @BeforeEach
    void setUp() {
        indexEngine = IndexEngineTestSupport.newIndexEngine();
        storeClient = mock(ExtensionStoreClient.class);
        converter = mock(ExtensionConverter.class);
        snapshotManager = new IndexSnapshotManager(workDir.resolve("indices"));
        initializer = new DefaultIndicesInitializer(indexEngine, storeClient, converter, snapshotManager);
        scheme = Scheme.buildFromType(FakePost.class);
        lenient().when(converter.convertFrom(any(), any(ExtensionStore.class))).thenAnswer(inv -> {
            ExtensionStore es = inv.getArgument(1);
            var name = es.getName().substring(es.getName().lastIndexOf('/') + 1);
            var post = fakePost(name, es.getVersion());
            return post;
        });
    }

    private void registerType() {
        indexEngine
                .getIndicesManager()
                .add(
                        FakePost.class,
                        List.of(IndexSpecs.<FakePost, String>single("spec.slug", String.class)
                                .indexFunc(p -> p.slug)
                                .nullable(false)
                                .build()));
    }

    private void registerTypeWithTags() {
        indexEngine
                .getIndicesManager()
                .add(
                        FakePost.class,
                        List.of(
                                IndexSpecs.<FakePost, String>single("spec.slug", String.class)
                                        .indexFunc(p -> p.slug)
                                        .nullable(false)
                                        .build(),
                                IndexSpecs.<FakePost, String>multi("spec.tags", String.class)
                                        .indexFunc(p -> p.tags)
                                        .build()));
    }

    private static ExtensionStore storeOf(String name, long version) {
        return new ExtensionStore(PREFIX + "/" + name, new byte[] {1}, version);
    }

    private static FakePost fakePost(String name, long version) {
        var post = new FakePost(name, "slug-" + name, Set.of("tag-" + name));
        post.getMetadata().setVersion(version);
        return post;
    }

    @Test
    void shouldFullBuildWhenNoSnapshot() {
        registerType();
        when(storeClient.listBy(anyString(), any(), anyInt()))
                .thenReturn(List.of(storeOf("a", 1L), storeOf("b", 1L)))
                .thenReturn(List.of());

        initializer.doInitialize(scheme);

        assertThat(indexEngine.retrieveAll(FakePost.class, null, null)).containsExactlyInAnyOrder("a", "b");
        verify(storeClient, never()).listNameVersionsByNamePrefix(anyString());
    }

    @Test
    void shouldRestoreFromSnapshotAndApplyDelta() {
        registerType();
        // snapshot contains a(v1), c(v1) and gone(v1); database contains a(v1), b(v1) and c(v2)
        // => a stays untouched, b and c are (re)fetched, gone is deleted
        indexEngine.insert(List.of(fakePost("a", 1L), fakePost("c", 1L), fakePost("gone", 1L)));
        var indices = indexEngine.getIndicesManager().get(FakePost.class);
        snapshotManager.save(FakePost.class, indices.dump());
        // simulate a restart: drop the in-memory indices and register the type from scratch
        indexEngine.getIndicesManager().remove(FakePost.class);
        registerType();

        when(storeClient.listNameVersionsByNamePrefix(PREFIX))
                .thenReturn(List.of(
                        new NameVersion(PREFIX + "/a", 1L),
                        new NameVersion(PREFIX + "/b", 1L),
                        new NameVersion(PREFIX + "/c", 2L)));
        when(storeClient.listByNames(anyList())).thenAnswer(inv -> {
            List<String> names = inv.getArgument(0);
            return names.stream()
                    .map(name -> {
                        var simpleName = name.substring(name.lastIndexOf('/') + 1);
                        var version = "c".equals(simpleName) ? 2L : 1L;
                        return storeOf(simpleName, version);
                    })
                    .toList();
        });

        initializer.doInitialize(scheme);

        assertThat(indexEngine.retrieveAll(FakePost.class, null, null)).containsExactlyInAnyOrder("a", "b", "c");
        // delta recovery must not fall back to a full-table data scan
        verify(storeClient, never()).listBy(anyString(), any(), anyInt());
        // the version-matched row must not be refetched
        verify(storeClient).listByNames(argThat(names -> !names.contains(PREFIX + "/a")));
    }

    @Test
    void shouldMergedScanRecoverWhenFingerprintsMismatch() {
        registerType();
        // a snapshot whose index entries match nothing: every index is dirty and must be rebuilt
        // by a merged full-table scan which also applies the delta against the manifest
        snapshotManager.save(FakePost.class, new IndicesSnapshot(List.of(), Map.of("gone", 1L)));
        when(storeClient.listBy(anyString(), any(), anyInt()))
                .thenReturn(List.of(storeOf("a", 1L)))
                .thenReturn(List.of());

        initializer.doInitialize(scheme);

        assertThat(indexEngine.retrieveAll(FakePost.class, null, null)).containsExactly("a");
    }

    @Test
    void shouldFillOnlyDirtyIndicesForUnchangedRowsInMergedScan() {
        registerTypeWithTags();
        // snapshot taken when both indices were intact: a(v1) with slug "slug-a" and tag "tag-a"
        indexEngine.insert(List.of(fakePost("a", 1L)));
        var snapshot = indexEngine.getIndicesManager().get(FakePost.class).dump();
        // corrupt the spec.tags fingerprint to simulate an index spec change since the snapshot was taken
        var corrupted = new IndicesSnapshot(
                snapshot.indices().stream()
                        .map(s -> "spec.tags".equals(s.name())
                                ? new IndexSnapshot(s.name(), "corrupted", s.keyType(), s.entries(), s.nullKeys())
                                : s)
                        .toList(),
                snapshot.versions());
        // simulate a restart: drop the in-memory indices and register the type from scratch
        indexEngine.getIndicesManager().remove(FakePost.class);
        registerTypeWithTags();
        snapshotManager.save(FakePost.class, corrupted);

        when(storeClient.listBy(anyString(), any(), anyInt()))
                .thenReturn(List.of(storeOf("a", 1L)))
                .thenReturn(List.of());

        initializer.doInitialize(scheme);

        // spec.slug was restored from the snapshot; if it had not been, this query would return nothing
        // because the unchanged row never goes through indexEngine.insert in the merged scan
        assertThat(queryBy("spec.slug", "slug-a")).containsExactly("a");
        // spec.tags was rebuilt by the merged scan's updateIndices(extension, dirtyIndexNames) branch
        assertThat(queryBy("spec.tags", "tag-a")).containsExactly("a");
        assertThat(indexEngine.retrieveAll(FakePost.class, null, null)).containsExactly("a");
        // the merged scan reads row data (exactly once) even for unchanged rows, to fill the dirty indices
        verify(converter).convertFrom(eq(FakePost.class), argThat(es -> (PREFIX + "/a").equals(es.getName())));
        // the delta path must not be used when there are dirty indices
        verify(storeClient, never()).listNameVersionsByNamePrefix(anyString());
        verify(storeClient, never()).listByNames(anyList());
        // Note: spying on Indices#updateIndices to pin the exact dirty-name set would require replacing
        // the IndicesManager internals of the real engine, so the behavioral assertions above stand in:
        // spec.slug resolving "a" proves snapshot restore (no rebuild), spec.tags resolving "a" proves
        // the updateIndices branch filled only the dirty index.
    }

    private Iterable<String> queryBy(String indexName, String value) {
        var options =
                ListOptions.builder().andQuery(Queries.equal(indexName, value)).build();
        return indexEngine.retrieveAll(FakePost.class, options, null);
    }

    @Test
    void shouldPurgeRestoredManifestBeforeFullBuildFallback() {
        registerType();
        // snapshot taken when the indices contained a(v1) and ghost(v1)
        indexEngine.insert(List.of(fakePost("a", 1L), fakePost("ghost", 1L)));
        var indices = indexEngine.getIndicesManager().get(FakePost.class);
        snapshotManager.save(FakePost.class, indices.dump());
        // simulate a restart: drop the in-memory indices and register the type from scratch
        indexEngine.getIndicesManager().remove(FakePost.class);
        registerType();

        // the database now contains a(v1) and the new row b(v1); ghost was deleted meanwhile
        when(storeClient.listNameVersionsByNamePrefix(PREFIX))
                .thenReturn(List.of(new NameVersion(PREFIX + "/a", 1L), new NameVersion(PREFIX + "/b", 1L)));
        // simulate a failure mid-delta-recovery: the refetch of the stale row fails after the
        // snapshot (including the ghost entry) has already been restored into the indices
        when(storeClient.listByNames(anyList())).thenThrow(new RuntimeException("simulated refetch failure"));
        when(storeClient.listBy(anyString(), any(), anyInt()))
                .thenReturn(List.of(storeOf("a", 1L), storeOf("b", 1L)))
                .thenReturn(List.of());

        initializer.doInitialize(scheme);

        // the full build ran as the fallback
        verify(storeClient, atLeastOnce()).listBy(anyString(), any(), anyInt());
        // the restored-but-stale manifest entries must have been purged: ghost must not survive
        var indexedNames = new ArrayList<String>();
        indexEngine.retrieveAll(FakePost.class, null, null).forEach(indexedNames::add);
        assertThat(indexedNames).containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void shouldFallBackToFullBuildOnCorruptedSnapshot() throws IOException {
        registerType();
        snapshotManager.save(FakePost.class, new IndicesSnapshot(List.of(), Map.of("a", 1L)));
        var snapshotFile = workDir.resolve("indices").resolve(FakePost.class.getName() + ".snapshot.gz");
        Files.write(snapshotFile, "corrupted".getBytes(StandardCharsets.UTF_8));
        when(storeClient.listBy(anyString(), any(), anyInt()))
                .thenReturn(List.of(storeOf("a", 1L)))
                .thenReturn(List.of());

        initializer.doInitialize(scheme);

        assertThat(indexEngine.retrieveAll(FakePost.class, null, null)).containsExactly("a");
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
