package run.halo.app.extension.index;

import org.springframework.util.Assert;

/**
 * <p>{@link IndexerTransaction} is a transactional interface for {@link Indexer} to ensure
 * consistency when {@link Indexer} indexes objects.</p>
 * <p>It is not supported to call {@link #begin()} twice without calling {@link #commit()} or
 * {@link #rollback()} in between and it is not supported to call one of {@link #commit()} or
 * {@link #rollback()} in different thread than {@link #begin()} was called.</p>
 *
 * @author guqing
 * @since 2.12.0
 */
public interface IndexerTransaction {
    void begin();

    void commit();

    void rollback();

    void add(ChangeRecord changeRecord);

    record ChangeRecord(IndexEntry indexEntry, String key, String value, boolean isAdd) {

        public ChangeRecord {
            Assert.notNull(indexEntry, "IndexEntry must not be null");
            Assert.notNull(key, "Key must not be null");
            Assert.notNull(value, "Value must not be null");
        }

        public static ChangeRecord onAdd(IndexEntry indexEntry, String key, String value) {
            return new ChangeRecord(indexEntry, key, value, true);
        }

        public static ChangeRecord onRemove(IndexEntry indexEntry, String key, String value) {
            return new ChangeRecord(indexEntry, key, value, false);
        }
    }
}
