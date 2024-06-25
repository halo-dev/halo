package run.halo.app.extension.index;

import static run.halo.app.extension.index.IndexerTransaction.ChangeRecord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.lang.NonNull;
import run.halo.app.extension.Extension;

/**
 * <p>A default implementation of {@link Indexer}.</p>
 * <p>It uses the {@link IndexEntryContainer} to store the index entries for the specified
 * {@link IndexDescriptor}s.</p>
 *
 * @author guqing
 * @since 2.12.0
 */
public class DefaultIndexer implements Indexer {
    private final ReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();

    private final List<IndexDescriptor> indexDescriptors;
    private final IndexEntryContainer indexEntries;

    /**
     * Constructs a new {@link DefaultIndexer} with the given {@link IndexDescriptor}s and
     * {@link IndexEntryContainer}.
     *
     * @param indexDescriptors the index descriptors.
     * @param oldIndexEntries must have the same size with the given descriptors
     */
    public DefaultIndexer(List<IndexDescriptor> indexDescriptors,
        IndexEntryContainer oldIndexEntries) {
        this.indexDescriptors = new ArrayList<>(indexDescriptors);
        this.indexEntries = new IndexEntryContainer();
        for (IndexEntry entry : oldIndexEntries) {
            this.indexEntries.add(entry);
        }
        for (IndexDescriptor indexDescriptor : indexDescriptors) {
            if (!indexDescriptor.isReady()) {
                throw new IllegalArgumentException(
                    "Index descriptor is not ready for: " + indexDescriptor.getSpec().getName());
            }
            if (!this.indexEntries.contains(indexDescriptor)) {
                throw new IllegalArgumentException(
                    "Index entry not found for: " + indexDescriptor.getSpec().getName());
            }
        }
    }

    static String getObjectKey(Extension extension) {
        return PrimaryKeySpecUtils.getObjectPrimaryKey(extension);
    }

    @Override
    public <E extends Extension> void indexRecord(E extension) {
        writeLock.lock();
        var transaction = new IndexerTransactionImpl();
        try {
            transaction.begin();
            doIndexRecord(extension).forEach(transaction::add);
            transaction.commit();
        } catch (Throwable e) {
            transaction.rollback();
            throw e;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public <E extends Extension> void updateRecord(E extension) {
        writeLock.lock();
        var transaction = new IndexerTransactionImpl();
        try {
            transaction.begin();
            unIndexRecord(getObjectKey(extension));
            indexRecord(extension);
            transaction.commit();
        } catch (Throwable e) {
            transaction.rollback();
            throw e;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void unIndexRecord(String extensionName) {
        writeLock.lock();
        var transaction = new IndexerTransactionImpl();
        try {
            transaction.begin();
            doUnIndexRecord(extensionName).forEach(transaction::add);
            transaction.commit();
        } catch (Throwable e) {
            transaction.rollback();
            throw e;
        } finally {
            writeLock.unlock();
        }
    }

    private List<ChangeRecord> doUnIndexRecord(String extensionName) {
        List<ChangeRecord> changeRecords = new ArrayList<>();
        for (IndexEntry indexEntry : indexEntries) {
            indexEntry.entries().forEach(records -> {
                var indexKey = records.getKey();
                var objectKey = records.getValue();
                if (objectKey.equals(extensionName)) {
                    changeRecords.add(ChangeRecord.onRemove(indexEntry, indexKey, objectKey));
                }
            });
        }
        return changeRecords;
    }

    private <E extends Extension> List<ChangeRecord> doIndexRecord(E extension) {
        List<ChangeRecord> changeRecords = new ArrayList<>();
        for (IndexDescriptor indexDescriptor : indexDescriptors) {
            var indexEntry = indexEntries.get(indexDescriptor);
            var indexFunc = indexDescriptor.getSpec().getIndexFunc();
            Set<String> indexKeys = indexFunc.getValues(extension);
            var objectKey = PrimaryKeySpecUtils.getObjectPrimaryKey(extension);
            for (String indexKey : indexKeys) {
                changeRecords.add(ChangeRecord.onAdd(indexEntry, indexKey, objectKey));
            }
        }
        return changeRecords;
    }

    @Override
    public IndexDescriptor findIndexByName(String name) {
        readLock.lock();
        try {
            return indexDescriptors.stream()
                .filter(descriptor -> descriptor.getSpec().getName().equals(name))
                .findFirst()
                .orElse(null);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public IndexEntry createIndexEntry(IndexDescriptor descriptor) {
        return new IndexEntryImpl(descriptor);
    }

    @Override
    public void removeIndexRecords(Function<IndexDescriptor, Boolean> matchFn) {
        writeLock.lock();
        try {
            var iterator = indexEntries.iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                if (BooleanUtils.isTrue(matchFn.apply(entry.getIndexDescriptor()))) {
                    iterator.remove();
                    entry.clear();
                    indexEntries.add(createIndexEntry(entry.getIndexDescriptor()));
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    @NonNull
    public IndexEntry getIndexEntry(String name) {
        readLock.lock();
        try {
            var indexDescriptor = findIndexByName(name);
            if (indexDescriptor == null) {
                throw new IllegalArgumentException(
                    "No index found for fieldPath [" + name + "], "
                        + "make sure you have created an index for this field.");
            }
            if (!indexDescriptor.isReady()) {
                throw new IllegalStateException(
                    "Index [" + name + "] is not ready, "
                        + "Please wait for more time or check the index status.");
            }
            return indexEntries.get(indexDescriptor);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Iterator<IndexEntry> readyIndexesIterator() {
        readLock.lock();
        try {
            var readyIndexes = new ArrayList<IndexEntry>();
            for (IndexEntry entry : indexEntries) {
                if (entry.getIndexDescriptor().isReady()) {
                    readyIndexes.add(entry);
                }
            }
            return readyIndexes.iterator();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Iterator<IndexEntry> allIndexesIterator() {
        readLock.lock();
        try {
            return indexEntries.iterator();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void acquireReadLock() {
        readLock.lock();
    }

    @Override
    public void releaseReadLock() {
        readLock.unlock();
    }
}
