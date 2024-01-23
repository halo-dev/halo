package run.halo.app.extension.index;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import org.springframework.lang.NonNull;

/**
 * <p>A container for {@link IndexEntry}s, it is used to store all {@link IndexEntry}s according
 * to the {@link IndexDescriptor}.</p>
 * <p>This class is thread-safe.</p>
 *
 * @author guqing
 * @see DefaultIndexer
 * @since 2.12.0
 */
public class IndexEntryContainer implements Iterable<IndexEntry> {
    private final ConcurrentMap<IndexDescriptor, IndexEntry> indexEntryMap;

    public IndexEntryContainer() {
        this.indexEntryMap = new ConcurrentHashMap<>();
    }

    /**
     * Add an {@link IndexEntry} to this container.
     *
     * @param entry the entry to add
     * @throws IllegalArgumentException if the entry already exists
     */
    public void add(IndexEntry entry) {
        IndexEntry existing = indexEntryMap.putIfAbsent(entry.getIndexDescriptor(), entry);
        if (existing != null) {
            throw new IllegalArgumentException(
                "Index entry already exists for " + entry.getIndexDescriptor());
        }
    }

    /**
     * Get the {@link IndexEntry} for the given {@link IndexDescriptor}.
     *
     * @param indexDescriptor the index descriptor
     * @return the index entry
     */
    public IndexEntry get(IndexDescriptor indexDescriptor) {
        return indexEntryMap.get(indexDescriptor);
    }

    public boolean contains(IndexDescriptor indexDescriptor) {
        return indexEntryMap.containsKey(indexDescriptor);
    }

    public void remove(IndexDescriptor indexDescriptor) {
        indexEntryMap.remove(indexDescriptor);
    }

    public int size() {
        return indexEntryMap.size();
    }

    @Override
    @NonNull
    public Iterator<IndexEntry> iterator() {
        return indexEntryMap.values().iterator();
    }

    @Override
    public void forEach(Consumer<? super IndexEntry> action) {
        indexEntryMap.values().forEach(action);
    }
}
