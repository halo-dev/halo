package run.halo.app.extension.index;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.Data;
import run.halo.app.infra.exception.DuplicateNameException;

@Data
public class IndexEntryImpl implements IndexEntry {
    private final ReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();

    private final IndexDescriptor indexDescriptor;
    private final ListMultimap<String, String> indexKeyObjectNamesMap;

    /**
     * Creates a new {@link IndexEntryImpl} for the given {@link IndexDescriptor}.
     *
     * @param indexDescriptor for which the {@link IndexEntryImpl} is created.
     */
    public IndexEntryImpl(IndexDescriptor indexDescriptor) {
        this.indexDescriptor = indexDescriptor;
        this.indexKeyObjectNamesMap = MultimapBuilder.treeKeys(keyComparator())
            .linkedListValues().build();
    }

    Comparator<String> keyComparator() {
        var order = indexDescriptor.getSpec().getOrder();
        if (IndexSpec.OrderType.ASC.equals(order)) {
            return KeyComparator.INSTANCE;
        } else if (IndexSpec.OrderType.DESC.equals(order)) {
            return Comparator.reverseOrder();
        } else {
            throw new IllegalArgumentException("Invalid order: " + order);
        }
    }

    static class KeyComparator implements Comparator<String> {
        public static final KeyComparator INSTANCE = new KeyComparator();

        @Override
        public int compare(String a, String b) {
            int i = 0;
            int j = 0;
            while (i < a.length() && j < b.length()) {
                if (Character.isDigit(a.charAt(i)) && Character.isDigit(b.charAt(j))) {
                    // handle number part
                    int num1 = 0;
                    int num2 = 0;
                    while (i < a.length() && Character.isDigit(a.charAt(i))) {
                        num1 = num1 * 10 + (a.charAt(i++) - '0');
                    }
                    while (j < b.length() && Character.isDigit(b.charAt(j))) {
                        num2 = num2 * 10 + (b.charAt(j++) - '0');
                    }
                    if (num1 != num2) {
                        return num1 - num2;
                    }
                } else if (a.charAt(i) != b.charAt(j)) {
                    // handle non-number part
                    return a.charAt(i) - b.charAt(j);
                } else {
                    i++;
                    j++;
                }
            }
            return a.length() - b.length();
        }
    }

    @Override
    public void acquireReadLock() {
        this.rwl.readLock().lock();
    }

    @Override
    public void releaseReadLock() {
        this.rwl.readLock().unlock();
    }

    @Override
    public void addEntry(List<String> keys, String objectName) {
        var isUnique = indexDescriptor.getSpec().isUnique();
        for (String key : keys) {
            writeLock.lock();
            try {
                if (isUnique && indexKeyObjectNamesMap.containsKey(key)) {
                    throw new DuplicateNameException(
                        "The value [%s] is already exists for unique index [%s].".formatted(
                            key,
                            indexDescriptor.getSpec().getName()),
                        null,
                        "problemDetail.index.duplicateKey",
                        new Object[] {key, indexDescriptor.getSpec().getName()});
                }
                this.indexKeyObjectNamesMap.put(key, objectName);
            } finally {
                writeLock.unlock();
            }
        }
    }

    @Override
    public void removeEntry(String indexedKey, String objectKey) {
        writeLock.lock();
        try {
            indexKeyObjectNamesMap.remove(indexedKey, objectKey);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void remove(String objectName) {
        writeLock.lock();
        try {
            indexKeyObjectNamesMap.values().removeIf(objectName::equals);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Set<String> indexedKeys() {
        readLock.lock();
        try {
            return indexKeyObjectNamesMap.keySet();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Collection<Map.Entry<String, String>> entries() {
        readLock.lock();
        try {
            return indexKeyObjectNamesMap.entries();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Collection<Map.Entry<String, String>> immutableEntries() {
        readLock.lock();
        try {
            return ImmutableListMultimap.copyOf(indexKeyObjectNamesMap).entries();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<String> getByIndexKey(String indexKey) {
        readLock.lock();
        try {
            return indexKeyObjectNamesMap.get(indexKey);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            indexKeyObjectNamesMap.clear();
        } finally {
            writeLock.unlock();
        }
    }
}
