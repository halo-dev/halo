package run.halo.app.extension.index;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import run.halo.app.extension.Extension;

/**
 * Default implementation of {@link Indices}.
 *
 * @param <E> the type of extension
 * @author johnniang
 * @since 2.22.0
 */
@Slf4j
class DefaultIndices<E extends Extension> implements Indices<E> {

    private final Map<String, Index<E, ?>> indexMap;

    private final Cache<String, ReadWriteLock> lockCache;

    private final ConcurrentMap<String, Long> versionMap = new ConcurrentHashMap<>();

    private volatile boolean closed;

    public DefaultIndices(List<Index<E, ?>> indices) {
        this.indexMap = indices.stream()
                .collect(Collectors.toMap(
                        Index::getName,
                        Function.identity(),
                        // keep existing in case of duplicate names
                        (existing, replacing) -> existing,
                        // keep insertion order
                        LinkedHashMap::new));
        this.lockCache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(1))
                .maximumSize(10_000)
                .build();
    }

    @Override
    public void close() throws IOException {
        closed = true;
        IOUtils.close(indexMap.values().toArray(Index[]::new));
        lockCache.invalidateAll();
    }

    @Override
    public void insert(E extension) {
        ensureNotClosed();
        // get primary key
        var primaryKey = extension.getMetadata().getName();
        var version = extension.getMetadata().getVersion();
        // the stale-version guard passes trivially for genuine creates (no recorded version) and skips
        // deferred inserts that are older than an already-applied update
        applyAll(extension, Index::prepareInsert, primaryKey, version, false, true);
    }

    @Override
    public void update(E extension) {
        ensureNotClosed();
        var primaryKey = extension.getMetadata().getName();
        var version = extension.getMetadata().getVersion();
        // the stale-version guard is checked under the per-primary-key write lock inside
        // applyAll, so that check-and-act is atomic
        applyAll(extension, Index::prepareUpdate, primaryKey, version, false, true);
    }

    @Override
    public void delete(E extension) {
        ensureNotClosed();
        var primaryKey = extension.getMetadata().getName();
        applyAll(extension, (index, ext) -> index.prepareDelete(primaryKey), primaryKey, null, true, false);
    }

    @Override
    public void deleteByName(String primaryKey) {
        ensureNotClosed();
        var lock = Objects.requireNonNull(lockCache.get(primaryKey, pk -> new ReentrantReadWriteLock()))
                .writeLock();
        var ops = new ArrayList<TransactionalOperation>();
        lock.lock();
        try {
            for (var index : indexMap.values()) {
                var op = index.prepareDelete(primaryKey);
                op.prepare();
                ops.add(op);
            }
            ops.forEach(TransactionalOperation::commit);
            versionMap.remove(primaryKey);
        } catch (Exception e) {
            ops.forEach(TransactionalOperation::rollback);
            throw e;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void updateIndices(E extension, Set<String> indexNames) {
        ensureNotClosed();
        var primaryKey = extension.getMetadata().getName();
        var lock = Objects.requireNonNull(lockCache.get(primaryKey, pk -> new ReentrantReadWriteLock()))
                .writeLock();
        var ops = new ArrayList<TransactionalOperation>();
        lock.lock();
        try {
            for (var index : indexMap.values()) {
                if (!indexNames.contains(index.getName())) {
                    continue;
                }
                var op = index.prepareUpdate(extension);
                op.prepare();
                ops.add(op);
            }
            ops.forEach(TransactionalOperation::commit);
            var version = extension.getMetadata().getVersion();
            if (version != null) {
                versionMap.put(primaryKey, version);
            }
        } catch (Exception e) {
            ops.forEach(TransactionalOperation::rollback);
            throw e;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public IndicesSnapshot dump() {
        // Capture the manifest BEFORE dumping indices: a torn dump then always satisfies
        // "manifest version <= index content", which delta recovery can only over-fetch from.
        var versions = Map.copyOf(versionMap);
        var snapshots = indexMap.values().stream().map(Index::dump).toList();
        return new IndicesSnapshot(snapshots, versions);
    }

    @Override
    public void restore(IndicesSnapshot snapshot) {
        for (var indexSnapshot : snapshot.indices()) {
            var index = indexMap.get(indexSnapshot.name());
            if (index != null) {
                index.restore(indexSnapshot);
            }
        }
        versionMap.putAll(snapshot.versions());
    }

    @Override
    public Map<String, String> currentFingerprints() {
        var result = new LinkedHashMap<String, String>();
        indexMap.values().forEach(index -> result.put(index.getName(), index.getFingerprint()));
        return result;
    }

    @Override
    public <K extends Comparable<K>> Index<E, K> getIndex(String indexName) {
        ensureNotClosed();
        var index = (Index<E, K>) indexMap.get(indexName);
        if (index == null) {
            throw new IllegalArgumentException("No index found with name: " + indexName);
        }
        return index;
    }

    private void applyAll(
            E extension,
            BiFunction<Index<E, ?>, E, TransactionalOperation> opFactory,
            String primaryKey,
            Long version,
            boolean deletion,
            boolean staleGuard) {
        var lock = Objects.requireNonNull(lockCache.get(primaryKey, pk -> new ReentrantReadWriteLock()))
                .writeLock();
        var ops = new ArrayList<TransactionalOperation>();
        lock.lock();
        try {
            if (staleGuard) {
                var recorded = versionMap.get(primaryKey);
                if (version != null && recorded != null && version < recorded) {
                    // stale update, skip it entirely
                    return;
                }
            }
            for (var index : indexMap.values()) {
                var op = opFactory.apply(index, extension);
                op.prepare();
                ops.add(op);
            }
            ops.forEach(TransactionalOperation::commit);
            // the version manifest is updated only after the index entries have committed
            if (deletion) {
                versionMap.remove(primaryKey);
            } else if (version != null) {
                versionMap.put(primaryKey, version);
            }
        } catch (Exception e) {
            log.warn("Failed to apply index operation for {} and trying to rollback", primaryKey, e);
            ops.forEach(TransactionalOperation::rollback);
            throw e;
        } finally {
            lock.unlock();
        }
    }

    private void ensureNotClosed() {
        if (closed) {
            throw new IllegalStateException("Indices has been closed");
        }
    }
}
