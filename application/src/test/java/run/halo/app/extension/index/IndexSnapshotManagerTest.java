package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.r2dbc.spi.ConnectionFactories;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactory;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import run.halo.app.extension.*;
import run.halo.app.extension.index.query.Queries;
import run.halo.app.extension.store.*;

class IndexSnapshotManagerTest {
    @TempDir
    Path workDir;

    ExtensionStoreRepository repository;
    R2dbcEntityTemplate template;
    TransactionalOperator tx;

    @BeforeEach
    void setUp() {
        var factory = ConnectionFactories.get("r2dbc:h2:mem:///snapshot-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        template = new R2dbcEntityTemplate(factory);
        template.getDatabaseClient()
                .sql("CREATE TABLE \"extensions\" (name VARCHAR(255) PRIMARY KEY, data BLOB, version BIGINT)")
                .then()
                .block();
        repository = new R2dbcRepositoryFactory(template).getRepository(ExtensionStoreRepository.class);
        tx = TransactionalOperator.create(new R2dbcTransactionManager(factory));
        seed(ConfigMap.class, "a", "original");
        seed(PluginRecord.class, "b", "plugin");
    }

    static String prefix(Class<? extends Extension> type) {
        return ExtensionStoreUtil.buildStoreNamePrefix(Scheme.buildFromType(type));
    }

    String checkpoint(Class<? extends Extension> type) {
        return IndexSnapshotManager.checkpointName(prefix(type));
    }

    byte[] data(Class<? extends ConfigMap> type, String name, String value) {
        try {
            var record = type.getDeclaredConstructor().newInstance();
            var metadata = new Metadata();
            metadata.setName(name);
            metadata.setCreationTimestamp(Instant.EPOCH);
            record.setMetadata(metadata);
            record.setData(Map.of("value", value, "other", value.toUpperCase()));
            return Unstructured.OBJECT_MAPPER.writeValueAsBytes(record);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    void seed(Class<? extends ConfigMap> type, String name, String value) {
        repository
                .save(new ExtensionStore(prefix(type) + "/" + name, data(type, name, value)))
                .block();
    }

    class Process {
        final IndexSnapshotWriteBarrier writes = new IndexSnapshotWriteBarrier();
        final IndexEngine engine = IndexEngineTestSupport.newIndexEngine();
        final List<Scheme> registered = new ArrayList<>();
        final AtomicInteger scans = new AtomicInteger();
        final ReactiveExtensionStoreClientImpl store =
                new ReactiveExtensionStoreClientImpl(repository, template, writes);
        final IndexSnapshotManager snapshots;

        Process() {
            var schemes = mock(SchemeManager.class);
            when(schemes.schemes()).thenReturn(registered);
            ObjectProvider<SchemeManager> provider = mock(ObjectProvider.class);
            when(provider.getObject()).thenReturn(schemes);
            snapshots =
                    new IndexSnapshotManager(workDir, repository, writes, engine, provider, mock(ObjectProvider.class));
        }

        <E extends ConfigMap> void start(Class<E> type) {
            start(type, 1, "value");
        }

        <E extends ConfigMap> void start(Class<E> type, int version, String field) {
            var scheme = Scheme.buildFromType(type);
            engine.getIndicesManager()
                    .add(
                            type,
                            List.of(IndexSpecs.<E, String>single("value", String.class)
                                    .indexFunc(e -> e.getData().get(field))
                                    .version(version)
                                    .build()));
            snapshots.initialize(scheme, () -> {
                scans.incrementAndGet();
                var records = repository
                        .findAllByNameStartingWith(prefix(type) + "/")
                        .map(row -> decode(type, row))
                        .collectList()
                        .block();
                engine.insert(records);
            });
            registered.add(scheme);
        }

        <E extends ConfigMap> E decode(Class<E> type, ExtensionStore row) {
            try {
                return Unstructured.OBJECT_MAPPER.readValue(row.getData(), type);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        void stop(Class<? extends ConfigMap> type) {
            var scheme = Scheme.buildFromType(type);
            snapshots.unregister(scheme, () -> {
                engine.getIndicesManager().remove(type);
                registered.remove(scheme);
            });
        }

        <E extends ConfigMap> Mono<ExtensionStore> update(Class<E> type, String name, String value) {
            var key = prefix(type) + "/" + name;
            return repository
                    .findById(key)
                    .flatMap(old -> store.update(key, old.getVersion(), data(type, name, value)))
                    .doOnNext(row -> engine.update(List.of(decode(type, row))));
        }

        List<String> query(Class<? extends ConfigMap> type, String value) {
            var names = engine.retrieveAll(
                    type,
                    ListOptions.builder()
                            .andQuery(Queries.equal("value", value))
                            .build(),
                    null);
            var result = new ArrayList<String>();
            names.forEach(result::add);
            return result;
        }
    }

    <T> Mono<T> indexed(Mono<T> write) {
        return write.as(tx::transactional).contextWrite(c -> c.put(IndexSnapshotWriteBarrier.INDEXED_WRITE, true));
    }

    @Test
    void sameProcessRestartMustSaveFreshDataAndAvoidScanning() {
        var process = new Process();
        process.start(ConfigMap.class);
        indexed(process.update(ConfigMap.class, "a", "updated")).block();
        process.stop(ConfigMap.class);
        assertThat(repository.existsById(checkpoint(ConfigMap.class)).block()).isTrue();
        process.start(ConfigMap.class);
        assertThat(process.scans).hasValue(1);
        assertThat(process.query(ConfigMap.class, "updated")).containsExactly("a");
        assertThat(process.query(ConfigMap.class, "original")).isEmpty();
        assertThat(repository.existsById(checkpoint(ConfigMap.class)).block()).isFalse();
        process.stop(ConfigMap.class);
        process.start(ConfigMap.class);
        assertThat(process.scans).hasValue(1);
    }

    @Test
    void newProcessOrCompatibleUpgradeMustReuseBothCoreAndPluginTypes() {
        var old = new Process();
        old.start(ConfigMap.class);
        old.start(PluginRecord.class);
        old.snapshots.saveOnShutdown();
        var upgraded = new Process();
        upgraded.start(ConfigMap.class);
        upgraded.start(PluginRecord.class);
        assertThat(upgraded.scans).hasValue(0);
        assertThat(upgraded.query(ConfigMap.class, "original")).containsExactly("a");
        assertThat(upgraded.query(PluginRecord.class, "plugin")).containsExactly("b");
        assertThat(Files.exists(workDir.resolve("indices/snapshots/" + PluginRecord.class.getName() + ".snapshot.gz")))
                .isTrue();
    }

    @Test
    void changedSemanticVersionMustOnlyRebuildAffectedType() {
        var old = new Process();
        old.start(ConfigMap.class);
        old.start(PluginRecord.class);
        old.snapshots.saveOnShutdown();
        var upgraded = new Process();
        upgraded.start(ConfigMap.class);
        upgraded.start(PluginRecord.class, 2, "other");
        assertThat(upgraded.scans).hasValue(1);
        assertThat(upgraded.query(PluginRecord.class, "PLUGIN")).containsExactly("b");
        assertThat(upgraded.query(PluginRecord.class, "plugin")).isEmpty();
        assertThat(upgraded.query(ConfigMap.class, "original")).containsExactly("a");
    }

    @Test
    void crashAfterConsumptionMustRebuildOnlyStartedTypes() {
        var old = new Process();
        old.start(ConfigMap.class);
        old.start(PluginRecord.class);
        old.snapshots.saveOnShutdown();
        var crashed = new Process();
        crashed.start(ConfigMap.class);
        var restarted = new Process();
        restarted.start(ConfigMap.class);
        restarted.start(PluginRecord.class);
        assertThat(restarted.scans).hasValue(1);
    }

    @Test
    void disabledPluginWritesAfterProcessRestartMustInvalidateEvenNestedResourceNames() {
        var old = new Process();
        old.start(PluginRecord.class);
        old.stop(PluginRecord.class);
        var next = new Process();
        next.store
                .create(prefix(PluginRecord.class) + "/nested/name", data(PluginRecord.class, "nested/name", "new"))
                .as(tx::transactional)
                .block();
        assertThat(repository.existsById(checkpoint(PluginRecord.class)).block())
                .isFalse();
        next.start(PluginRecord.class);
        assertThat(next.scans).hasValue(1);
        assertThat(next.query(PluginRecord.class, "new")).containsExactly("nested/name");
    }

    @Test
    void rolledBackDisabledWriteMustNotCacheUncommittedCheckpointConsumption() {
        var process = new Process();
        process.start(PluginRecord.class);
        process.stop(PluginRecord.class);
        assertThatThrownBy(() -> process.store
                        .create(
                                prefix(PluginRecord.class) + "/rolled-back",
                                data(PluginRecord.class, "rolled-back", "x"))
                        .then(Mono.error(new IllegalStateException("rollback")))
                        .as(tx::transactional)
                        .block())
                .hasMessage("rollback");
        assertThat(repository.existsById(checkpoint(PluginRecord.class)).block())
                .isTrue();
        process.store
                .create(prefix(PluginRecord.class) + "/committed", data(PluginRecord.class, "committed", "new"))
                .as(tx::transactional)
                .block();
        assertThat(repository.existsById(checkpoint(PluginRecord.class)).block())
                .isFalse();
        process.start(PluginRecord.class);
        assertThat(process.query(PluginRecord.class, "new")).containsExactly("committed");
    }

    @Test
    void captureMustDrainTransactionsAndOnlyGateTargetType() throws Exception {
        var process = new Process();
        process.start(ConfigMap.class);
        process.start(PluginRecord.class);
        var stored = Sinks.<Void>one();
        var finish = Sinks.<Void>one();
        var operation = indexed(process.update(PluginRecord.class, "b", "updated")
                        .doOnNext(ignored -> stored.tryEmitEmpty())
                        .then(finish.asMono()))
                .toFuture();
        stored.asMono().block(Duration.ofSeconds(5));
        var stopped = CompletableFuture.runAsync(() -> process.stop(PluginRecord.class));
        assertThatThrownBy(() -> stopped.get(150, TimeUnit.MILLISECONDS))
                .isInstanceOf(java.util.concurrent.TimeoutException.class);
        indexed(process.update(ConfigMap.class, "a", "unrelated")).block();
        assertThatThrownBy(() ->
                        indexed(process.update(PluginRecord.class, "b", "late")).block())
                .hasMessageContaining("draining");
        finish.tryEmitEmpty();
        operation.get(5, TimeUnit.SECONDS);
        stopped.get(5, TimeUnit.SECONDS);
        process.start(PluginRecord.class);
        assertThat(process.scans).hasValue(2);
        assertThat(process.query(PluginRecord.class, "updated")).containsExactly("b");
        assertThat(process.query(ConfigMap.class, "unrelated")).containsExactly("a");
    }

    @Test
    void rollbackAfterIndexMutationMustDisableOnlyAffectedTypeAndRebuildRepairsEligibility() {
        var process = new Process();
        process.start(ConfigMap.class);
        process.start(PluginRecord.class);
        assertThatThrownBy(() -> indexed(process.update(PluginRecord.class, "b", "rolled-back")
                                .then(Mono.error(new IllegalStateException("rollback"))))
                        .block())
                .hasMessage("rollback");
        process.snapshots.saveOnShutdown();
        assertThat(repository.existsById(checkpoint(PluginRecord.class)).block())
                .isFalse();
        assertThat(repository.existsById(checkpoint(ConfigMap.class)).block()).isTrue();
        process.stop(PluginRecord.class);
        process.start(PluginRecord.class);
        assertThat(process.query(PluginRecord.class, "plugin")).containsExactly("b");
        assertThat(process.query(PluginRecord.class, "rolled-back")).isEmpty();
        process.stop(PluginRecord.class);
        assertThat(repository.existsById(checkpoint(PluginRecord.class)).block())
                .isTrue();
    }

    @Test
    void conflictBeforeEmissionMustPreserveEligibility() {
        var process = new Process();
        process.start(PluginRecord.class);
        var old = repository.findById(prefix(PluginRecord.class) + "/b").block();
        indexed(process.update(PluginRecord.class, "b", "updated")).block();
        assertThatThrownBy(() -> indexed(process.store.update(old.getName(), old.getVersion(), old.getData()))
                        .block())
                .isInstanceOf(org.springframework.dao.OptimisticLockingFailureException.class);
        process.stop(PluginRecord.class);
        process.start(PluginRecord.class);
        assertThat(process.scans).hasValue(1);
        assertThat(process.query(PluginRecord.class, "updated")).containsExactly("b");
    }

    @Test
    void directTransactionalStoreWritesMustNotPublishStaleInMemoryIndices() {
        var process = new Process();
        process.start(PluginRecord.class);
        process.store
                .create(prefix(PluginRecord.class) + "/direct", data(PluginRecord.class, "direct", "new"))
                .as(tx::transactional)
                .block();
        process.stop(PluginRecord.class);
        assertThat(repository.existsById(checkpoint(PluginRecord.class)).block())
                .isFalse();
        process.start(PluginRecord.class);
        assertThat(process.query(PluginRecord.class, "new")).containsExactly("direct");
    }

    @Test
    void cancellationAndDrainTimeoutMustNotPublishCheckpoint() throws Exception {
        var process = new Process();
        process.start(PluginRecord.class);
        var stored = Sinks.<Void>one();
        var operation = indexed(process.update(PluginRecord.class, "b", "cancelled")
                        .doOnNext(ignored -> stored.tryEmitEmpty())
                        .then(Mono.never()))
                .subscribe();
        stored.asMono().block(Duration.ofSeconds(5));
        assertThatThrownBy(() -> process.writes.freeze(prefix(PluginRecord.class), Duration.ofMillis(10)))
                .hasMessageContaining("Timed out");
        operation.dispose();
        try (var ignored = process.writes.freeze(prefix(PluginRecord.class), Duration.ofSeconds(5))) {
            assertThat(process.writes.isReusable(prefix(PluginRecord.class))).isFalse();
        }
        process.stop(PluginRecord.class);
        assertThat(repository.existsById(checkpoint(PluginRecord.class)).block())
                .isFalse();
    }

    @Test
    void unknownCompletionMustInvalidateEvenWithoutStoreEmission() {
        var process = new Process();
        process.start(PluginRecord.class);
        org.springframework.transaction.reactive.TransactionSynchronizationManager.forCurrentTransaction()
                .flatMap(manager -> {
                    manager.initSynchronization();
                    return process.writes
                            .track(prefix(PluginRecord.class) + "/unknown", repository, Mono::empty)
                            .then(Mono.defer(
                                    () -> reactor.core.publisher.Flux.fromIterable(manager.getSynchronizations())
                                            .concatMap(sync -> sync.afterCompletion(
                                                    org.springframework.transaction.reactive.TransactionSynchronization
                                                            .STATUS_UNKNOWN))
                                            .then()));
                })
                .contextWrite(
                        org.springframework.transaction.reactive.TransactionContextManager.createTransactionContext())
                .block();
        process.stop(PluginRecord.class);
        assertThat(repository.existsById(checkpoint(PluginRecord.class)).block())
                .isFalse();
    }

    @Test
    void corruptFileAndForeignDatabaseTokenMustRebuild() throws Exception {
        var process = new Process();
        process.start(ConfigMap.class);
        process.start(PluginRecord.class);
        process.snapshots.saveOnShutdown();
        Files.writeString(workDir.resolve("indices/snapshots/" + ConfigMap.class.getName() + ".snapshot.gz"), "broken");
        var checkpoint = repository.findById(checkpoint(PluginRecord.class)).block();
        var text = new String(checkpoint.getData(), java.nio.charset.StandardCharsets.UTF_8);
        checkpoint.setData((text.substring(0, text.indexOf('\n') + 1) + UUID.randomUUID())
                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        repository.save(checkpoint).block();
        var next = new Process();
        next.start(ConfigMap.class);
        next.start(PluginRecord.class);
        assertThat(next.scans).hasValue(2);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(
            strings = {"missing", "empty", "truncated", "crc", "token", "tmp-only"})
    void damagedCacheMustRepairItselfAndRestoreOnFollowingRestart(String damage) throws Exception {
        var first = new Process();
        first.start(PluginRecord.class);
        first.stop(PluginRecord.class);
        var file = workDir.resolve("indices/snapshots/" + PluginRecord.class.getName() + ".snapshot.gz");
        var bytes = Files.readAllBytes(file);
        switch (damage) {
            case "missing" -> Files.delete(file);
            case "empty" -> Files.write(file, new byte[0]);
            case "truncated" -> Files.write(file, java.util.Arrays.copyOf(bytes, bytes.length / 2));
            case "crc" -> {
                bytes[bytes.length - 1] ^= 1;
                Files.write(file, bytes);
            }
            case "token" -> {
                var snapshot = IndexSnapshotCodec.read(new java.io.ByteArrayInputStream(bytes));
                try (var output = Files.newOutputStream(file)) {
                    IndexSnapshotCodec.write(new IndicesSnapshot("foreign", snapshot.indices()), output);
                }
            }
            case "tmp-only" -> Files.move(file, file.resolveSibling(file.getFileName() + ".tmp"));
            default -> throw new AssertionError(damage);
        }
        var recovery = new Process();
        recovery.start(PluginRecord.class);
        assertThat(recovery.scans).hasValue(1);
        assertThat(recovery.query(PluginRecord.class, "plugin")).containsExactly("b");
        recovery.stop(PluginRecord.class);
        var warm = new Process();
        warm.start(PluginRecord.class);
        assertThat(warm.scans).hasValue(0);
        assertThat(warm.query(PluginRecord.class, "plugin")).containsExactly("b");
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"read-only", "symlink", "directory"})
    void staleTemporaryPathMustNotPreventFutureSnapshotsOrOverwriteItsTarget(String kind) throws Exception {
        var process = new Process();
        process.start(PluginRecord.class);
        var directory = workDir.resolve("indices/snapshots");
        Files.createDirectories(directory);
        var temporary = directory.resolve(PluginRecord.class.getName() + ".snapshot.gz.tmp");
        var unrelated = workDir.resolve("unrelated.txt");
        Files.writeString(unrelated, "must remain unchanged");
        var abandoned = Files.createTempFile(directory, PluginRecord.class.getName() + ".snapshot.gz.", ".tmp");
        Files.writeString(abandoned, "incomplete snapshot");
        if (kind.equals("symlink")) {
            Files.createSymbolicLink(temporary, unrelated);
        } else if (kind.equals("directory")) {
            Files.createDirectory(temporary);
            Files.writeString(temporary.resolve("unrelated"), "keep");
        } else {
            Files.writeString(temporary, "abandoned");
            Files.setPosixFilePermissions(
                    temporary, java.nio.file.attribute.PosixFilePermissions.fromString("r--------"));
        }
        process.stop(PluginRecord.class);
        assertThat(Files.readString(unrelated)).isEqualTo("must remain unchanged");
        assertThat(abandoned).doesNotExist();
        if (kind.equals("directory")) {
            assertThat(Files.readString(temporary.resolve("unrelated"))).isEqualTo("keep");
        } else {
            assertThat(Files.exists(temporary, java.nio.file.LinkOption.NOFOLLOW_LINKS))
                    .isFalse();
        }
        assertThat(repository.existsById(checkpoint(PluginRecord.class)).block())
                .isTrue();
        var next = new Process();
        next.start(PluginRecord.class);
        assertThat(next.scans).hasValue(0);
        assertThat(next.query(PluginRecord.class, "plugin")).containsExactly("b");
    }

    @Test
    void sharedEngineCompatibilityChangeMustRebuild() {
        var process = new Process();
        process.start(PluginRecord.class);
        process.stop(PluginRecord.class);
        var marker = repository.findById(checkpoint(PluginRecord.class)).block();
        marker.setData(new String(marker.getData(), java.nio.charset.StandardCharsets.UTF_8)
                .replaceFirst("^[^|]+\\|", "previous-engine|")
                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        repository.save(marker).block();
        process.start(PluginRecord.class);
        assertThat(process.scans).hasValue(2);
    }

    @Test
    void backupInvalidationMustWaitForCaptureAndPreventFurtherPublication() throws Exception {
        var process = new Process();
        process.start(PluginRecord.class);
        CompletableFuture<Void> invalidation;
        try (var ignored = process.writes.freeze(prefix(PluginRecord.class), Duration.ofSeconds(5))) {
            invalidation = process.writes.invalidateAll().toFuture();
            assertThatThrownBy(() -> invalidation.get(150, TimeUnit.MILLISECONDS))
                    .isInstanceOf(java.util.concurrent.TimeoutException.class);
        }
        invalidation.get(5, TimeUnit.SECONDS);
        process.stop(PluginRecord.class);
        assertThat(repository.existsById(checkpoint(PluginRecord.class)).block())
                .isFalse();
    }

    @Test
    void checkpointConsumptionFailureMustAbortBeforeServingIndices() {
        var process = new Process();
        var failing = mock(ExtensionStoreRepository.class);
        when(failing.findById(checkpoint(PluginRecord.class))).thenReturn(Mono.empty());
        doReturn(Mono.error(new IllegalStateException("database unavailable")))
                .when(failing)
                .deleteById(checkpoint(PluginRecord.class));
        var manager = new IndexSnapshotManager(
                workDir,
                failing,
                process.writes,
                process.engine,
                mock(ObjectProvider.class),
                mock(ObjectProvider.class));
        assertThatThrownBy(() ->
                        manager.initialize(Scheme.buildFromType(PluginRecord.class), () -> fail("must not build")))
                .hasMessage("database unavailable");
        assertThat(process.writes.isReusable(prefix(PluginRecord.class))).isFalse();
    }

    @Test
    void persistenceFailureMustStillAllowUnregistrationWithoutPublishingCheckpoint() throws Exception {
        var process = new Process();
        process.start(PluginRecord.class);
        Files.createDirectories(workDir.resolve("indices"));
        Files.writeString(workDir.resolve("indices/snapshots"), "not a directory");
        process.stop(PluginRecord.class);
        assertThat(process.registered).isEmpty();
        assertThat(repository.existsById(checkpoint(PluginRecord.class)).block())
                .isFalse();
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(
            strings = {"added", "removed", "renamed", "unique", "nullable", "keyType", "multi"})
    void changedRegistrationMustRebuild(String change) {
        var old = new Process();
        old.start(PluginRecord.class);
        old.stop(PluginRecord.class);
        var next = new Process();
        var definitions = new ArrayList<ValueIndexSpec<PluginRecord, ?>>();
        if (!change.equals("removed")) {
            if (change.equals("keyType")) {
                definitions.add(IndexSpecs.<PluginRecord, Integer>single("value", Integer.class)
                        .indexFunc(e -> e.getData().get("value").length())
                        .build());
            } else if (change.equals("multi")) {
                definitions.add(IndexSpecs.<PluginRecord, String>multi("value", String.class)
                        .indexFunc(e -> java.util.Set.of(e.getData().get("value")))
                        .build());
            } else {
                definitions.add(IndexSpecs.<PluginRecord, String>single(
                                change.equals("renamed") ? "newValue" : "value", String.class)
                        .indexFunc(e -> e.getData().get("value"))
                        .unique(change.equals("unique"))
                        .nullable(!change.equals("nullable"))
                        .build());
            }
        }
        if (change.equals("added")) {
            definitions.add(IndexSpecs.<PluginRecord, String>single("extra", String.class)
                    .indexFunc(e -> e.getData().get("other"))
                    .build());
        }
        next.engine.getIndicesManager().add(PluginRecord.class, definitions);
        next.snapshots.initialize(Scheme.buildFromType(PluginRecord.class), () -> {
            next.scans.incrementAndGet();
            next.engine.insert(repository
                    .findAllByNameStartingWith(prefix(PluginRecord.class) + "/")
                    .map(row -> next.decode(PluginRecord.class, row))
                    .collectList()
                    .block());
        });
        assertThat(next.scans).hasValue(1);
        assertThat(next.engine.retrieveAll(PluginRecord.class, null, null)).containsExactly("b");
    }

    @Test
    void missingCheckpointMustNotTrustFilesFromAnotherDatabase() {
        var old = new Process();
        old.start(PluginRecord.class);
        old.stop(PluginRecord.class);
        repository.deleteById(checkpoint(PluginRecord.class)).block();
        var next = new Process();
        next.start(PluginRecord.class);
        assertThat(next.scans).hasValue(1);
    }

    @Test
    void lateWriteAfterRootCaptureMustInvalidateThePublishedCheckpoint() {
        var process = new Process();
        process.start(PluginRecord.class);
        process.snapshots.saveOnShutdown();
        indexed(process.update(PluginRecord.class, "b", "late")).block();
        assertThat(repository.existsById(checkpoint(PluginRecord.class)).block())
                .isFalse();
        var next = new Process();
        next.start(PluginRecord.class);
        assertThat(next.scans).hasValue(1);
        assertThat(next.query(PluginRecord.class, "late")).containsExactly("b");
    }

    @Test
    void nonTransactionalWriteMustInvalidateMemoryAndDiskEligibility() {
        var process = new Process();
        process.start(PluginRecord.class);
        process.snapshots.saveOnShutdown();
        process.store
                .create(prefix(PluginRecord.class) + "/direct", data(PluginRecord.class, "direct", "new"))
                .block();
        process.stop(PluginRecord.class);
        assertThat(repository.existsById(checkpoint(PluginRecord.class)).block())
                .isFalse();
        process.start(PluginRecord.class);
        assertThat(process.query(PluginRecord.class, "new")).containsExactly("direct");
    }

    @Test
    void rootCaptureFollowedBySchemeDestructionMustKeepThePublishedGeneration() {
        var process = new Process();
        process.start(ConfigMap.class);
        process.snapshots.saveOnShutdown();
        var published = repository.findById(checkpoint(ConfigMap.class)).block().getData();
        process.stop(ConfigMap.class);
        assertThat(repository.findById(checkpoint(ConfigMap.class)).block().getData())
                .isEqualTo(published);
        var next = new Process();
        next.start(ConfigMap.class);
        assertThat(next.scans).hasValue(0);
        assertThat(next.query(ConfigMap.class, "original")).containsExactly("a");
    }

    @Test
    void rootLifecycleMustStopPluginsBeforeSavingRemainingCoreTypes() {
        var process = new Process();
        process.start(ConfigMap.class);
        process.start(PluginRecord.class);
        var schemeManager = mock(SchemeManager.class);
        when(schemeManager.schemes()).thenReturn(process.registered);
        ObjectProvider<SchemeManager> schemeProvider = mock(ObjectProvider.class);
        when(schemeProvider.getObject()).thenReturn(schemeManager);
        var plugins = mock(run.halo.app.plugin.SpringPluginManager.class);
        ObjectProvider<run.halo.app.plugin.SpringPluginManager> pluginProvider = mock(ObjectProvider.class);
        when(pluginProvider.getIfAvailable()).thenReturn(plugins);
        doAnswer(ignored -> {
                    assertThat(repository
                                    .existsById(checkpoint(ConfigMap.class))
                                    .block())
                            .isFalse();
                    process.stop(PluginRecord.class);
                    return null;
                })
                .when(plugins)
                .stopPlugins();
        var lifecycle = new IndexSnapshotManager(
                workDir, repository, process.writes, process.engine, schemeProvider, pluginProvider);
        try (var context = new org.springframework.context.support.GenericApplicationContext()) {
            lifecycle.setApplicationContext(context);
            lifecycle.start();
            lifecycle.onApplicationEvent(new org.springframework.boot.context.event.ApplicationReadyEvent(
                    new org.springframework.boot.SpringApplication(), new String[0], context, Duration.ZERO));
            lifecycle.stop();
        }
        verify(plugins).stopPlugins();
        assertThat(lifecycle.isRunning()).isFalse();
        assertThat(repository.existsById(checkpoint(ConfigMap.class)).block()).isTrue();
        assertThat(repository.existsById(checkpoint(PluginRecord.class)).block())
                .isTrue();
        assertThat(lifecycle.getPhase()).isLessThan(run.halo.app.infra.InitializationPhase.CONTROLLERS.getPhase());
    }

    @Test
    void committedCheckpointWithLostAcknowledgementMustBeInvalidatedByNextWrite() {
        var process = new Process();
        process.start(PluginRecord.class);
        var uncertain = org.mockito.Mockito.mock(
                ExtensionStoreRepository.class, org.mockito.AdditionalAnswers.delegatesTo(repository));
        doAnswer(call -> {
                    ExtensionStore row = call.getArgument(0);
                    var saved = repository.save(row);
                    return row.getData().length == 0
                            ? saved
                            : saved.flatMap(
                                    ignored -> Mono.error(new IllegalStateException("lost commit acknowledgement")));
                })
                .when(uncertain)
                .save(any(ExtensionStore.class));
        var manager = snapshotManager(process, uncertain);
        manager.unregister(
                Scheme.buildFromType(PluginRecord.class),
                () -> process.engine.getIndicesManager().remove(PluginRecord.class));
        assertThat(repository.existsById(checkpoint(PluginRecord.class)).block())
                .isTrue();
        dormantUpdate(process, "after-error");
        assertThat(repository.existsById(checkpoint(PluginRecord.class)).block())
                .isFalse();
        var next = new Process();
        next.start(PluginRecord.class);
        assertThat(next.scans).hasValue(1);
        assertThat(next.query(PluginRecord.class, "after-error")).containsExactly("b");
        assertThat(next.query(PluginRecord.class, "plugin")).isEmpty();
    }

    @Test
    void delayedPublicationMustNotRecreateCheckpointAfterAWrite() {
        var process = new Process();
        process.start(PluginRecord.class);
        var delayed = new java.util.concurrent.atomic.AtomicReference<ExtensionStore>();
        var uncertain = org.mockito.Mockito.mock(
                ExtensionStoreRepository.class, org.mockito.AdditionalAnswers.delegatesTo(repository));
        doAnswer(call -> {
                    ExtensionStore row = call.getArgument(0);
                    if (row.getData().length == 0) {
                        return repository.save(row);
                    }
                    delayed.set(row);
                    return Mono.error(new IllegalStateException("publication timed out before DB completion"));
                })
                .when(uncertain)
                .save(any(ExtensionStore.class));
        snapshotManager(process, uncertain)
                .unregister(
                        Scheme.buildFromType(PluginRecord.class),
                        () -> process.engine.getIndicesManager().remove(PluginRecord.class));
        dormantUpdate(process, "after-timeout");
        assertThatThrownBy(() -> repository.save(delayed.get()).block())
                .isInstanceOf(org.springframework.dao.OptimisticLockingFailureException.class);
        assertThat(repository.existsById(checkpoint(PluginRecord.class)).block())
                .isFalse();
        var next = new Process();
        next.start(PluginRecord.class);
        assertThat(next.scans).hasValue(1);
        assertThat(next.query(PluginRecord.class, "after-timeout")).containsExactly("b");
    }

    @Test
    void lateReservationAfterAWriteMustNeverAuthorizeRestoration() {
        var process = new Process();
        process.start(PluginRecord.class);
        var delayed = new java.util.concurrent.atomic.AtomicReference<ExtensionStore>();
        var uncertain = org.mockito.Mockito.mock(
                ExtensionStoreRepository.class, org.mockito.AdditionalAnswers.delegatesTo(repository));
        doAnswer(call -> {
                    delayed.set(call.getArgument(0));
                    return Mono.error(new IllegalStateException("reservation timed out before DB completion"));
                })
                .when(uncertain)
                .save(any(ExtensionStore.class));
        snapshotManager(process, uncertain)
                .unregister(
                        Scheme.buildFromType(PluginRecord.class),
                        () -> process.engine.getIndicesManager().remove(PluginRecord.class));
        dormantUpdate(process, "after-reservation-timeout");
        repository.save(delayed.get()).block();
        var next = new Process();
        next.start(PluginRecord.class);
        assertThat(next.scans).hasValue(1);
        assertThat(next.query(PluginRecord.class, "after-reservation-timeout")).containsExactly("b");
        assertThat(next.query(PluginRecord.class, "plugin")).isEmpty();
    }

    @Test
    void lateReservationAfterReinitializationMustNotPreventTheNextSnapshot() {
        var process = new Process();
        process.start(PluginRecord.class);
        var delayed = new java.util.concurrent.atomic.AtomicReference<ExtensionStore>();
        var uncertain = mock(ExtensionStoreRepository.class, org.mockito.AdditionalAnswers.delegatesTo(repository));
        doAnswer(call -> {
                    delayed.set(call.getArgument(0));
                    return Mono.error(new IllegalStateException("reservation timed out"));
                })
                .when(uncertain)
                .save(any(ExtensionStore.class));
        var scheme = Scheme.buildFromType(PluginRecord.class);
        snapshotManager(process, uncertain).unregister(scheme, () -> {
            process.engine.getIndicesManager().remove(PluginRecord.class);
            process.registered.remove(scheme);
        });
        process.start(PluginRecord.class);
        repository.save(delayed.get()).block();
        indexed(process.update(PluginRecord.class, "b", "latest")).block();
        process.stop(PluginRecord.class);
        var next = new Process();
        next.start(PluginRecord.class);
        assertThat(next.scans).hasValue(0);
        assertThat(next.query(PluginRecord.class, "latest")).containsExactly("b");
        assertThat(next.query(PluginRecord.class, "plugin")).isEmpty();
    }

    @Test
    void longTextKeyShouldRecoverWithoutScanning() {
        var process = new Process();
        process.start(PluginRecord.class);
        var text = "评论😀".repeat(30000);
        indexed(process.update(PluginRecord.class, "b", text)).block();
        process.stop(PluginRecord.class);
        var next = new Process();
        next.start(PluginRecord.class);
        assertThat(next.scans).hasValue(0);
        assertThat(next.query(PluginRecord.class, text)).containsExactly("b");
    }

    private void dormantUpdate(Process process, String value) {
        var key = prefix(PluginRecord.class) + "/b";
        var row = repository.findById(key).block();
        process.store
                .update(key, row.getVersion(), data(PluginRecord.class, "b", value))
                .as(tx::transactional)
                .block();
    }

    private IndexSnapshotManager snapshotManager(Process process, ExtensionStoreRepository checkpointRepository) {
        var schemes = mock(SchemeManager.class);
        when(schemes.schemes()).thenReturn(process.registered);
        ObjectProvider<SchemeManager> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(schemes);
        return new IndexSnapshotManager(
                workDir, checkpointRepository, process.writes, process.engine, provider, mock(ObjectProvider.class));
    }

    @GVK(group = "snapshot.test", version = "v1", kind = "PluginRecord", plural = "records", singular = "record")
    public static class PluginRecord extends ConfigMap {}
}
