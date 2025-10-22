package run.halo.app.extension.index;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.lang.NonNull;
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

    private volatile boolean closed;

    public DefaultIndices(List<Index<E, ?>> indices) {
        this.indexMap = indices.stream()
            .collect(Collectors.toMap(
                Index::getName,
                Function.identity(),
                // keep existing in case of duplicate names
                (existing, replacing) -> existing,
                // keep insertion order
                LinkedHashMap::new)
            );
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
        var lock = Objects.requireNonNull(
            lockCache.get(primaryKey, pk -> new ReentrantReadWriteLock())
        ).writeLock();
        var ops = new ArrayList<TransactionalOperation>();
        lock.lock();
        try {
            for (var index : indexMap.values()) {
                var op = index.prepareInsert(extension);
                op.prepare();
                ops.add(op);
            }
            ops.forEach(TransactionalOperation::commit);
        } catch (Exception e) {
            log.warn("Failed to insert extension {} and trying to rollback", primaryKey, e);
            ops.forEach(TransactionalOperation::rollback);
            throw e;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void update(E extension) {
        ensureNotClosed();
        var primaryKey = extension.getMetadata().getName();
        var lock = Objects.requireNonNull(
            lockCache.get(primaryKey, pk -> new ReentrantReadWriteLock())
        ).writeLock();
        var updaters = new ArrayList<TransactionalOperation>();
        lock.lock();
        try {
            for (var index : indexMap.values()) {
                var updater = index.prepareUpdate(extension);
                updater.prepare();
                updaters.add(updater);
            }
            updaters.forEach(TransactionalOperation::commit);
        } catch (Exception e) {
            updaters.forEach(TransactionalOperation::rollback);
            throw e;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void delete(E extension) {
        ensureNotClosed();
        var primaryKey = extension.getMetadata().getName();
        var lock = Objects.requireNonNull(
            lockCache.get(primaryKey, pk -> new ReentrantReadWriteLock())
        ).writeLock();
        var updaters = new ArrayList<TransactionalOperation>();
        lock.lock();
        try {
            for (var index : indexMap.values()) {
                var updater = index.prepareDelete(primaryKey);
                updater.prepare();
                updaters.add(updater);
            }
            updaters.forEach(TransactionalOperation::commit);
        } catch (Exception e) {
            updaters.forEach(TransactionalOperation::rollback);
            throw e;
        } finally {
            lock.unlock();
        }
    }

    @Override
    @NonNull
    public <K extends Comparable<K>> Index<E, K> getIndex(String indexName) {
        ensureNotClosed();
        var index = (Index<E, K>) indexMap.get(indexName);
        if (index == null) {
            throw new IllegalArgumentException("No index found with name: " + indexName);
        }
        return index;
    }

    private void ensureNotClosed() {
        if (closed) {
            throw new IllegalStateException("Indices has been closed");
        }
    }
}
