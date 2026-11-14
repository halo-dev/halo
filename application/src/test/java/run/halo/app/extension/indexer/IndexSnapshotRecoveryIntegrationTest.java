package run.halo.app.extension.indexer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionConverter;
import run.halo.app.extension.GVK;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.index.IndexEngine;
import run.halo.app.extension.index.IndexEngineTestSupport;
import run.halo.app.extension.index.IndexSnapshotManager;
import run.halo.app.extension.index.IndexSpecs;
import run.halo.app.extension.index.query.Queries;
import run.halo.app.extension.store.ExtensionStore;
import run.halo.app.extension.store.ExtensionStoreClient;
import run.halo.app.extension.store.NameVersion;

/**
 * End-to-end integration test for index snapshot persistence: full build, snapshot save on "shutdown", snapshot restore
 * plus delta recovery after "restart", and merged-scan recovery when an index spec is added. Uses a real
 * {@link IndexEngine} (via {@link IndexEngineTestSupport}), a real {@link IndexSnapshotManager} backed by a temp
 * directory, an in-memory fake {@link ExtensionStoreClient} and a mock {@link ExtensionConverter}; no Spring container
 * involved.
 */
class IndexSnapshotRecoveryIntegrationTest {

    private static final String PREFIX = "/registry/fakeposts";

    @TempDir
    Path workDir;

    InMemoryExtensionStoreClient storeClient;

    ExtensionConverter converter;

    IndexSnapshotManager snapshotManager;

    Scheme scheme;

    @BeforeEach
    void setUp() {
        storeClient = new InMemoryExtensionStoreClient();
        converter = mock(ExtensionConverter.class);
        snapshotManager = new IndexSnapshotManager(workDir.resolve("indices"));
        scheme = Scheme.buildFromType(FakePost.class);
        lenient().when(converter.convertFrom(any(), any(ExtensionStore.class))).thenAnswer(inv -> {
            ExtensionStore es = inv.getArgument(1);
            var name = es.getName().substring(es.getName().lastIndexOf('/') + 1);
            return fakePost(name, es.getVersion());
        });
        storeClient.put(storeOf("a", 1L));
        storeClient.put(storeOf("b", 1L));
        storeClient.put(storeOf("c", 1L));
    }

    private Environment newEnvironment() {
        var engine = IndexEngineTestSupport.newIndexEngine();
        var initializer = new DefaultIndicesInitializer(engine, storeClient, converter, snapshotManager);
        return new Environment(engine, initializer);
    }

    private record Environment(IndexEngine engine, DefaultIndicesInitializer initializer) {}

    private static void registerSlugSpec(IndexEngine engine) {
        engine.getIndicesManager()
                .add(
                        FakePost.class,
                        List.of(IndexSpecs.<FakePost, String>single("spec.slug", String.class)
                                .indexFunc(p -> p.slug)
                                .nullable(false)
                                .build()));
    }

    private static void registerSlugAndTagsSpecs(IndexEngine engine) {
        engine.getIndicesManager()
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

    /** Simulates the snapshot save done by DefaultSchemeManager.unregister on shutdown. */
    private void saveSnapshot(IndexEngine engine) {
        var indices = engine.getIndicesManager().get(FakePost.class);
        snapshotManager.save(FakePost.class, indices.dump());
    }

    private static ExtensionStore storeOf(String name, long version) {
        return new ExtensionStore(PREFIX + "/" + name, new byte[] {1}, version);
    }

    private static FakePost fakePost(String name, long version) {
        var post = new FakePost(name, name + "-slug-v" + version, Set.of("tag-" + name));
        post.getMetadata().setVersion(version);
        return post;
    }

    private static Iterable<String> queryBy(IndexEngine engine, String indexName, String value) {
        var options =
                ListOptions.builder().andQuery(Queries.equal(indexName, value)).build();
        return engine.retrieveAll(FakePost.class, options, null);
    }

    @Test
    void shouldRestoreFromSnapshotWithoutFullScanAfterRestart() {
        // first boot: no snapshot, full build from the store
        var env1 = newEnvironment();
        registerSlugSpec(env1.engine());
        env1.initializer().doInitialize(scheme);
        assertThat(storeClient.listByCalls).isPositive();
        assertThat(env1.engine().retrieveAll(FakePost.class, null, null)).containsExactlyInAnyOrder("a", "b", "c");
        assertThat(queryBy(env1.engine(), "spec.slug", "a-slug-v1")).containsExactly("a");
        assertThatCode(() -> env1.engine().getIndicesManager().awaitReady(FakePost.class))
                .doesNotThrowAnyException();

        // shutdown: save the snapshot like DefaultSchemeManager.unregister does
        saveSnapshot(env1.engine());

        // restart: a fresh engine must restore from the snapshot without any full-table scan
        storeClient.resetCounters();
        var env2 = newEnvironment();
        registerSlugSpec(env2.engine());
        env2.initializer().doInitialize(scheme);

        assertThat(storeClient.listByCalls).isZero();
        assertThat(storeClient.listNameVersionsCalls).isEqualTo(1);
        // query results are identical to before the restart
        assertThat(env2.engine().retrieveAll(FakePost.class, null, null)).containsExactlyInAnyOrder("a", "b", "c");
        assertThat(queryBy(env2.engine(), "spec.slug", "a-slug-v1")).containsExactly("a");
        assertThat(queryBy(env2.engine(), "spec.slug", "b-slug-v1")).containsExactly("b");
        assertThatCode(() -> env2.engine().getIndicesManager().awaitReady(FakePost.class))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldDeltaRecoverAfterExternalMutations() {
        var env1 = newEnvironment();
        registerSlugSpec(env1.engine());
        env1.initializer().doInitialize(scheme);
        saveSnapshot(env1.engine());

        // while "down", the database is mutated externally: b is updated (v2), a is deleted, d is added
        storeClient.put(storeOf("b", 2L));
        storeClient.remove(PREFIX + "/a");
        storeClient.put(storeOf("d", 1L));

        storeClient.resetCounters();
        var env2 = newEnvironment();
        registerSlugSpec(env2.engine());
        env2.initializer().doInitialize(scheme);

        // delta recovery must not fall back to a full-table data scan
        assertThat(storeClient.listByCalls).isZero();
        assertThat(storeClient.listNameVersionsCalls).isEqualTo(1);
        assertThat(env2.engine().retrieveAll(FakePost.class, null, null)).containsExactlyInAnyOrder("b", "c", "d");
        // the updated row was reindexed with its new content
        assertThat(queryBy(env2.engine(), "spec.slug", "b-slug-v2")).containsExactly("b");
        assertThat(queryBy(env2.engine(), "spec.slug", "b-slug-v1")).isEmpty();
        // the deleted row is gone, the added row is present
        assertThat(queryBy(env2.engine(), "spec.slug", "a-slug-v1")).isEmpty();
        assertThat(queryBy(env2.engine(), "spec.slug", "d-slug-v1")).containsExactly("d");
        // untouched rows remain intact
        assertThat(queryBy(env2.engine(), "spec.slug", "c-slug-v1")).containsExactly("c");
    }

    @Test
    void shouldMergedScanRecoverWhenIndexSpecAdded() {
        var env1 = newEnvironment();
        registerSlugSpec(env1.engine());
        env1.initializer().doInitialize(scheme);
        saveSnapshot(env1.engine());

        // restart with an added index spec: the new index has no matching fingerprint in the snapshot,
        // so a merged full-table scan rebuilds it while the matched one is restored from the snapshot
        storeClient.resetCounters();
        var env2 = newEnvironment();
        registerSlugAndTagsSpecs(env2.engine());
        env2.initializer().doInitialize(scheme);

        assertThat(storeClient.listByCalls).isPositive();
        assertThat(storeClient.listNameVersionsCalls).isZero();
        assertThat(env2.engine().retrieveAll(FakePost.class, null, null)).containsExactlyInAnyOrder("a", "b", "c");
        // the new index answers queries
        assertThat(queryBy(env2.engine(), "spec.tags", "tag-b")).containsExactly("b");
        // the unchanged index was restored from the snapshot and still answers queries
        assertThat(queryBy(env2.engine(), "spec.slug", "a-slug-v1")).containsExactly("a");
    }

    static class InMemoryExtensionStoreClient implements ExtensionStoreClient {

        private final NavigableMap<String, ExtensionStore> stores = new TreeMap<>();

        int listByCalls;

        int listNameVersionsCalls;

        void resetCounters() {
            listByCalls = 0;
            listNameVersionsCalls = 0;
        }

        void put(ExtensionStore store) {
            stores.put(store.getName(), store);
        }

        void remove(String name) {
            stores.remove(name);
        }

        @Override
        public List<ExtensionStore> listBy(String prefix, String nameCursor, int limit) {
            listByCalls++;
            var from = nameCursor == null ? prefix + "/" : nameCursor;
            return stores.tailMap(from, false).values().stream()
                    .filter(es -> es.getName().startsWith(prefix + "/"))
                    .limit(limit)
                    .toList();
        }

        @Override
        public List<ExtensionStore> listByNames(List<String> names) {
            return names.stream().map(stores::get).filter(Objects::nonNull).toList();
        }

        @Override
        public List<NameVersion> listNameVersionsByNamePrefix(String prefix) {
            listNameVersionsCalls++;
            return stores.values().stream()
                    .filter(es -> es.getName().startsWith(prefix + "/"))
                    .map(es -> new NameVersion(es.getName(), es.getVersion()))
                    .toList();
        }

        @Override
        public List<ExtensionStore> listByNamePrefix(String prefix) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Page<ExtensionStore> listByNamePrefix(String prefix, Pageable pageable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<ExtensionStore> fetchByName(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ExtensionStore create(String name, byte[] data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ExtensionStore update(String name, Long version, byte[] data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ExtensionStore delete(String name, Long version) {
            throw new UnsupportedOperationException();
        }
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
